package com.grievance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

import com.grievance.exception.ResourceNotFoundException;
import com.grievance.exception.StorageException;
import com.grievance.model.Grievance;
import com.grievance.repository.GrievanceRepository;
import com.mongodb.client.gridfs.model.GridFSFile;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileStorageServiceTest {

    @Mock
    private ReactiveGridFsTemplate gridFsTemplate;
    @Mock
    private GrievanceRepository grievanceRepository;

    @InjectMocks
    private FileStorageService fileStorageService;

    private FilePart imagePart;
    private HttpHeaders imageHeaders;
    private DataBuffer content;

    @BeforeEach
    void setup() {
        imagePart = org.mockito.Mockito.mock(FilePart.class);
        imageHeaders = new HttpHeaders();
        imageHeaders.setContentType(MediaType.IMAGE_PNG);
        content = new DefaultDataBufferFactory().wrap("hello".getBytes());

        when(imagePart.filename()).thenReturn("image.png");
        when(imagePart.headers()).thenReturn(imageHeaders);
        when(imagePart.content()).thenReturn(Flux.just(content));
    }

    @Test
    void uploadStoresFileAndReturnsId() {
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(new Grievance()));
        when(gridFsTemplate.store(any(Flux.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.just(new ObjectId("507f1f77bcf86cd799439011")));

        StepVerifier.create(fileStorageService.upload(imagePart, "g1", "user-1"))
                .expectNext("507f1f77bcf86cd799439011")
                .verifyComplete();
    }

    @Test
    void uploadAcceptsSmallPositiveContentLength() {
        imageHeaders.setContentLength(1024);
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(new Grievance()));
        when(gridFsTemplate.store(any(Flux.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.just(new ObjectId("507f1f77bcf86cd799439012")));

        StepVerifier.create(fileStorageService.upload(imagePart, "g1", "user-1"))
                .expectNext("507f1f77bcf86cd799439012")
                .verifyComplete();
    }

    @Test
    void uploadRejectsMissingUploader() {
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(new Grievance()));

        assertThatThrownBy(() -> fileStorageService.upload(imagePart, "g1", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        imageHeaders.setContentType(MediaType.TEXT_PLAIN);

        assertThatThrownBy(() -> fileStorageService.upload(imagePart, "g1", "user-1"))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void uploadRejectsMissingFields() {
        assertThatThrownBy(() -> fileStorageService.upload(null, "g1", "user-1"))
                .isInstanceOf(StorageException.class);

        imageHeaders.setContentType(MediaType.IMAGE_JPEG);
        imageHeaders.setContentLength(25 * 1024 * 1024L);

        assertThatThrownBy(() -> fileStorageService.upload(imagePart, "", ""))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void uploadRejectsMissingContentType() {
        imageHeaders.setContentType(null);

        assertThatThrownBy(() -> fileStorageService.upload(imagePart, "g1", "user-1"))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void uploadAllowsPdfDocuments() {
        imageHeaders.setContentType(MediaType.APPLICATION_PDF);
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(new Grievance()));
        when(gridFsTemplate.store(any(Flux.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.just(new ObjectId("507f1f77bcf86cd799439011")));

        StepVerifier.create(fileStorageService.upload(imagePart, "g1", "user-1"))
                .expectNext("507f1f77bcf86cd799439011")
                .verifyComplete();
    }

    @Test
    void uploadRejectsWhenStreamExceedsLimit() {
        byte[] firstChunk = new byte[15 * 1024 * 1024];
        byte[] secondChunk = new byte[8 * 1024 * 1024];
        DataBuffer buffer1 = new DefaultDataBufferFactory().wrap(firstChunk);
        DataBuffer buffer2 = new DefaultDataBufferFactory().wrap(secondChunk);
        when(imagePart.content()).thenReturn(Flux.just(buffer1, buffer2));
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(new Grievance()));
        when(gridFsTemplate.store(any(Flux.class), any(String.class), any(Map.class)))
                .thenAnswer(invocation -> {
                    Flux<DataBuffer> data = invocation.getArgument(0);
                    return data.then(Mono.just(new ObjectId("507f1f77bcf86cd799439011")));
                });

        StepVerifier.create(fileStorageService.upload(imagePart, "g1", "user-1"))
                .expectError(StorageException.class)
                .verify();
    }

    @Test
    void downloadReturnsResource() {
        GridFSFile gridFile = new GridFSFile(
                new org.bson.BsonObjectId(new ObjectId("507f1f77bcf86cd799439011")),
                "image.png",
                5L,
                0,
                new java.util.Date(),
                new Document("contentType", "image/png"));

        ReactiveGridFsResource resource = org.mockito.Mockito.mock(ReactiveGridFsResource.class);
        when(resource.getFilename()).thenReturn("image.png");
        when(resource.getDownloadStream()).thenReturn(Flux.just(content));

        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(Mono.just(gridFile));
        when(gridFsTemplate.getResource(gridFile)).thenReturn(Mono.just(resource));

        StepVerifier.create(fileStorageService.download("507f1f77bcf86cd799439011"))
                .assertNext(res -> assertThat(res.getFilename()).isEqualTo("image.png"))
                .verifyComplete();
    }

    @Test
    void downloadRejectsBadId() {
        assertThatThrownBy(() -> fileStorageService.download("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByGrievanceValidatesIdAndMapsMetadata() {
        Document meta = new Document();
        meta.put("contentType", "image/png");
        meta.put("uploadedBy", "user-1");
        GridFSFile file = new GridFSFile(
                new org.bson.BsonObjectId(new ObjectId("507f1f77bcf86cd799439011")),
                "file.png",
                10L,
                0,
                new java.util.Date(),
                meta);
        when(gridFsTemplate.find(any(Query.class))).thenReturn(Flux.just(file));

        StepVerifier.create(fileStorageService.listByGrievance("g1"))
                .assertNext(metaData -> {
                    assertThat(metaData.getId()).isEqualTo("507f1f77bcf86cd799439011");
                    assertThat(metaData.getFileName()).isEqualTo("file.png");
                    assertThat(metaData.getUploadedBy()).isEqualTo("user-1");
                })
                .verifyComplete();

        StepVerifier.create(fileStorageService.listByGrievance(""))
                .expectError(StorageException.class)
                .verify();
    }
}
