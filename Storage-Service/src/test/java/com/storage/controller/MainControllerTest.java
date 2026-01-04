package com.storage.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.storage.model.FileMetadata;
import com.storage.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

class MainControllerTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MainController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadReturnsFileIdMap() {
        FilePart part = mock(FilePart.class);
        when(storageService.upload(part, "g1", "u1")).thenReturn(Mono.just("file123"));

        StepVerifier.create(controller.upload(part, "g1", "u1"))
                .assertNext(map -> assertThat(map).isEqualTo(Map.of("fileId", "file123")))
                .verifyComplete();
    }

    @Test
    void downloadBuildsResponseEntity() {
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("data".getBytes());
        org.springframework.data.mongodb.gridfs.ReactiveGridFsResource resource =
                mock(org.springframework.data.mongodb.gridfs.ReactiveGridFsResource.class);
        when(resource.getFilename()).thenReturn("file.txt");
        when(resource.getDownloadStream()).thenReturn(Flux.just(buffer));

        when(storageService.download("id1")).thenReturn(Mono.just(resource));

        StepVerifier.create(controller.download("id1"))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(entity.getHeaders().getFirst("Content-Disposition")).contains("file.txt");
                    assertThat(entity.getBody()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void listByGrievanceDelegatesToService() {
        FileMetadata metadata = new FileMetadata();
        metadata.setId("id");

        when(storageService.listByGrievance("g1")).thenReturn(Flux.just(metadata));

        StepVerifier.create(controller.listByGrievance("g1"))
                .expectNext(metadata)
                .verifyComplete();
    }
}
