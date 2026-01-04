package com.storage.service;

import com.storage.client.GrievanceClient;
import com.storage.exception.ResourceNotFoundException;
import com.storage.exception.StorageException;
import com.storage.model.FileMetadata;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_DOC_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final ReactiveGridFsTemplate gridFsTemplate;
    private final GrievanceClient grievanceClient;

    public StorageService(ReactiveGridFsTemplate gridFsTemplate, GrievanceClient grievanceClient) {
        this.gridFsTemplate = gridFsTemplate;
        this.grievanceClient = grievanceClient;
    }

    // to upload a file
    public Mono<String> upload(FilePart file, String grievanceId, String uploadedBy) {

        validateUploadInputs(file, grievanceId, uploadedBy);
        MediaType contentType = resolveContentType(file);

        Flux<DataBuffer> validatedContent = enforceSizeLimit(file.content());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("grievanceId", grievanceId);
        metadata.put("uploadedBy", uploadedBy);
        metadata.put("contentType", contentType.toString());

        return grievanceClient.validateGrievance(grievanceId)
                .then(
                        gridFsTemplate.store(validatedContent, file.filename(), metadata)
                )
                .map(ObjectId::toHexString);
    }

    // to download a file
    public Mono<ReactiveGridFsResource> download(String fileId) {
        ObjectId objectId = parseObjectId(fileId);

        return gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File not found")))
                .flatMap(gridFsTemplate::getResource);
    }

    // list files by grievance
    public Flux<FileMetadata> listByGrievance(String grievanceId) {
        if (!StringUtils.hasText(grievanceId)) {
            return Flux.error(new StorageException("grievanceId is required"));
        }

        return gridFsTemplate.find(
                        Query.query(Criteria.where("metadata.grievanceId").is(grievanceId))
                )
                .map(file -> {
                    FileMetadata meta = new FileMetadata();
                    meta.setId(file.getObjectId().toHexString());
                    meta.setFileName(file.getFilename());
                    meta.setContentType(
                            file.getMetadata().getString("contentType")
                    );
                    meta.setGrievanceId(grievanceId);
                    meta.setUploadedBy(
                            file.getMetadata().getString("uploadedBy")
                    );
                    return meta;
                });
    }

    private void validateUploadInputs(FilePart file, String grievanceId, String uploadedBy) {
        if (file == null) {
            throw new StorageException("File is required");
        }
        if (!StringUtils.hasText(grievanceId)) {
            throw new StorageException("grievanceId is required");
        }
        if (!StringUtils.hasText(uploadedBy)) {
            throw new StorageException("uploadedBy is required");
        }
        long contentLength = file.headers().getContentLength();
        if (contentLength > 0 && contentLength > MAX_FILE_SIZE_BYTES) {
            throw new StorageException("File exceeds maximum size of 20MB");
        }
    }

    private MediaType resolveContentType(FilePart file) {
        MediaType mediaType = file.headers().getContentType();
        if (mediaType == null) {
            throw new StorageException("File type is missing");
        }

        String type = mediaType.toString().toLowerCase();
        if (type.startsWith("image/") || ALLOWED_DOC_TYPES.contains(type)) {
            return mediaType;
        }

        throw new StorageException("Only pdf, image, or doc files are allowed");
    }

    private Flux<DataBuffer> enforceSizeLimit(Flux<DataBuffer> content) {
        AtomicLong totalSize = new AtomicLong(0);

        return content.handle((dataBuffer, sink) -> {
            long newSize = totalSize.addAndGet(dataBuffer.readableByteCount());
            if (newSize > MAX_FILE_SIZE_BYTES) {
                DataBufferUtils.release(dataBuffer);
                sink.error(new StorageException("File exceeds maximum size of 20MB"));
                return;
            }
            sink.next(dataBuffer);
        });
    }

    private ObjectId parseObjectId(String fileId) {
        try {
            return new ObjectId(fileId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("File not found");
        }
    }
}
