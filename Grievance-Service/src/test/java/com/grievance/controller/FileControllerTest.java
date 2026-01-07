package com.grievance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;

import com.grievance.model.FileMetadata;
import com.grievance.service.FileStorageService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileController controller;

    @Test
    void uploadDelegatesToService() {
        FilePart part = org.mockito.Mockito.mock(FilePart.class);
        when(fileStorageService.upload(part, "g1", "user-1")).thenReturn(Mono.just("file-id"));

        StepVerifier.create(controller.upload(part, "g1", "user-1"))
                .assertNext(map -> assertThat(map).containsEntry("fileId", "file-id"))
                .verifyComplete();
    }

    @Test
    void downloadReturnsResponseEntity() {
        var data = new DefaultDataBufferFactory().wrap("data".getBytes());
        var resource = org.mockito.Mockito.mock(org.springframework.data.mongodb.gridfs.ReactiveGridFsResource.class);
        when(resource.getDownloadStream()).thenReturn(Flux.just(data));
        when(resource.getFilename()).thenReturn("file.txt");
        when(fileStorageService.download("file-id")).thenReturn(Mono.just(resource));

        StepVerifier.create(controller.download("file-id"))
                .assertNext(response -> {
                    ResponseEntity<?> entity = response;
                    assertThat(entity.getHeaders().getContentDisposition().getFilename()).isEqualTo("file.txt");
                })
                .verifyComplete();
    }

    @Test
    void listByGrievanceDelegates() {
        FileMetadata meta = new FileMetadata();
        meta.setId("id");
        when(fileStorageService.listByGrievance("g1")).thenReturn(Flux.just(meta));

        StepVerifier.create(controller.listByGrievance("g1"))
                .expectNext(meta)
                .verifyComplete();
    }
}
