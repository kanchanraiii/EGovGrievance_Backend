package com.storage.service;

import com.storage.exception.StorageException;
import com.storage.model.FileMetadata;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class StorageService {

    private final ReactiveGridFsTemplate gridFsTemplate;

    public StorageService(ReactiveGridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    
    // to upload a file
    public Mono<String> upload(
            FilePart file,
            String grievanceId,
            String uploadedBy) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("grievanceId", grievanceId);
        metadata.put("uploadedBy", uploadedBy);
        metadata.put("contentType", file.headers().getContentType().toString());

        return gridFsTemplate.store(
                file.content(),
                file.filename(),
                metadata
        ).map(ObjectId::toHexString);
    }

   
    // tp download a file
    public Mono<GridFsResource> download(String fileId) {
        return gridFsTemplate.findOne(
                        Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
                )
                .switchIfEmpty(Mono.error(new StorageException("File not found")))
                .flatMap(gridFsTemplate::getResource);
    }

    
    // list files by grievance
    public Flux<FileMetadata> listByGrievance(String grievanceId) {
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
}
