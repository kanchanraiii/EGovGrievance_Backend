package com.grievance.controller;

import com.grievance.model.FileMetadata;
import com.grievance.service.FileStorageService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Validated
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, String>> upload(
            @RequestPart("file") FilePart file,
            @RequestPart("grievanceId") @NotBlank String grievanceId,
            @RequestPart("uploadedBy") @NotBlank String uploadedBy) {

        return fileStorageService.upload(file, grievanceId, uploadedBy)
                .map(id -> Map.of("fileId", id));
    }

    @GetMapping("/{fileId}")
    public Mono<org.springframework.http.ResponseEntity<Flux<DataBuffer>>> download(
            @PathVariable String fileId) {

        return fileStorageService.download(fileId)
                .map(resource -> {
                    Flux<DataBuffer> fileContent = resource.getDownloadStream();

                    return org.springframework.http.ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + resource.getFilename() + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(fileContent);
                });
    }

    @GetMapping("/grievance/{grievanceId}")
    public Flux<FileMetadata> listByGrievance(@PathVariable String grievanceId) {
        return fileStorageService.listByGrievance(grievanceId);
    }
}
