package com.storage.controller;

import com.storage.model.FileMetadata;
import com.storage.service.StorageService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/files")
@Validated
public class MainController {

    private final StorageService storageService;

    public MainController(StorageService storageService) {
        this.storageService = storageService;
    }

    // to upload a file
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> upload(
            @RequestPart("file") FilePart file,
            @RequestPart("grievanceId") @NotBlank String grievanceId,
            @RequestPart("uploadedBy") @NotBlank String uploadedBy) {

        return storageService.upload(file, grievanceId, uploadedBy);
    }

    
    // to download a file
    @GetMapping("/{fileId}")
    public Mono<org.springframework.http.ResponseEntity<Flux<DataBuffer>>> download(
            @PathVariable String fileId) {

        return storageService.download(fileId)
                .map(resource -> {
                    Flux<DataBuffer> fileContent = resource.getDownloadStream();

                    return org.springframework.http.ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + resource.getFilename() + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(fileContent);
                });
    }

     
    // to list files by grievance
    @GetMapping("/grievance/{grievanceId}")
    public Flux<FileMetadata> listByGrievance(
            @PathVariable String grievanceId) {

        return storageService.listByGrievance(grievanceId);
    }
}
