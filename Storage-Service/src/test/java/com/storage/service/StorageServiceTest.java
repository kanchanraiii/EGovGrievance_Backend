package com.storage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;

import com.storage.client.GrievanceClient;
import com.storage.exception.ResourceNotFoundException;
import com.storage.exception.StorageException;
import com.storage.model.FileMetadata;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StorageServiceTest {

    @Mock
    private ReactiveGridFsTemplate gridFsTemplate;
    @Mock
    private GrievanceClient grievanceClient;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(gridFsTemplate, grievanceClient);
    }

    @Test
    void uploadStoresFileWhenValid() {
        FilePart file = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(0);
        when(file.headers()).thenReturn(headers);
        when(file.filename()).thenReturn("image.jpg");

        DataBuffer buffer = new DefaultDataBufferFactory().wrap("data".getBytes());
        when(file.content()).thenReturn(Flux.just(buffer));
        when(grievanceClient.validateGrievance("g1")).thenReturn(Mono.empty());

        ObjectId storedId = new ObjectId();
        when(gridFsTemplate.store(any(Flux.class), eq("image.jpg"), any(Map.class))).thenReturn(Mono.just(storedId));

        StepVerifier.create(storageService.upload(file, "g1", "user1"))
                .expectNext(storedId.toHexString())
                .verifyComplete();

        verify(grievanceClient).validateGrievance("g1");
        verify(gridFsTemplate).store(any(Flux.class), eq("image.jpg"), any(Map.class));
    }

    @Test
    void uploadRejectsLargeFileViaHeader() {
        FilePart file = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(25 * 1024 * 1024);
        when(file.headers()).thenReturn(headers);
        when(file.filename()).thenReturn("big.jpg");

        assertThrows(StorageException.class, () -> storageService.upload(file, "g1", "user1"));
        verifyNoInteractions(gridFsTemplate);
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        FilePart file = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentLength(0);
        when(file.headers()).thenReturn(headers);
        when(file.filename()).thenReturn("text.txt");
        when(file.content()).thenReturn(Flux.empty());

        assertThrows(StorageException.class, () -> storageService.upload(file, "g1", "user1"));
        verifyNoInteractions(gridFsTemplate);
    }

    @Test
    void uploadRejectsMissingFields() {
        assertThrows(StorageException.class, () -> storageService.upload(null, "g1", "user1"));
        FilePart file = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        when(file.headers()).thenReturn(headers);
        when(file.filename()).thenReturn("name");
        when(file.content()).thenReturn(Flux.empty());

        assertThrows(StorageException.class, () -> storageService.upload(file, "", "user1"));
        assertThrows(StorageException.class, () -> storageService.upload(file, "g1", ""));
    }

    @Test
    void downloadRejectsInvalidObjectId() {
        assertThrows(ResourceNotFoundException.class, () -> storageService.download("not-object-id"));
    }

    @Test
    void downloadErrorsWhenNotFound() {
        ObjectId id = new ObjectId();
        when(gridFsTemplate.findOne(any())).thenReturn(Mono.empty());

        StepVerifier.create(storageService.download(id.toHexString()))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void downloadReturnsResource() {
        ObjectId id = new ObjectId();
        GridFSFile gridFsFile = new GridFSFile(new BsonObjectId(id), "name", 0, 0, new Date(), new Document());
        ReactiveGridFsResource resource = mock(ReactiveGridFsResource.class);

        when(gridFsTemplate.findOne(any())).thenReturn(Mono.just(gridFsFile));
        when(gridFsTemplate.getResource(gridFsFile)).thenReturn(Mono.just(resource));

        StepVerifier.create(storageService.download(id.toHexString()))
                .expectNext(resource)
                .verifyComplete();
    }

    @Test
    void listByGrievanceRejectsBlankId() {
        StepVerifier.create(storageService.listByGrievance(""))
                .expectError(StorageException.class)
                .verify();
    }

    @Test
    void listByGrievanceMapsMetadata() {
        ObjectId id = new ObjectId();
        Document metadata = new Document("contentType", "image/jpeg").append("uploadedBy", "user1");
        GridFSFile gridFsFile = new GridFSFile(new BsonObjectId(id), "name.jpg", 0, 0, new Date(), metadata);

        when(gridFsTemplate.find(any())).thenReturn(Flux.just(gridFsFile));

        StepVerifier.create(storageService.listByGrievance("g1"))
                .assertNext(meta -> {
                    assertThat(meta.getId()).isEqualTo(id.toHexString());
                    assertThat(meta.getFileName()).isEqualTo("name.jpg");
                    assertThat(meta.getContentType()).isEqualTo("image/jpeg");
                    assertThat(meta.getGrievanceId()).isEqualTo("g1");
                    assertThat(meta.getUploadedBy()).isEqualTo("user1");
                })
                .verifyComplete();
    }
}
