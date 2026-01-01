package com.storage.controller;

import com.storage.model.FileMetadata;
import com.storage.service.StorageService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/files")
public class MainController {

    private final StorageService storageService;

    public MainController(StorageService storageService) {
        this.storageService = storageService;
    }

    // to upload a file
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> upload(
            @RequestPart("file") FilePart file,
            @RequestPart("grievanceId") String grievanceId,
            @RequestPart("uploadedBy") String uploadedBy) {

        return storageService.upload(file, grievanceId, uploadedBy);
    }

    
    // to download a file
    @GetMapping("/{fileId}")
    public Mono<org.springframework.http.ResponseEntity<Flux<DataBuffer>>> download(
            @PathVariable String fileId) {

        return storageService.download(fileId)
                .map(resource ->
                        org.springframework.http.ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                        "attachment; filename=\"" + resource.getFilename() + "\"")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(resource.getDownloadStream())
                );
    }

     
    // to list files by grievance
    @GetMapping("/grievance/{grievanceId}")
    public Flux<FileMetadata> listByGrievance(
            @PathVariable String grievanceId) {

        return storageService.listByGrievance(grievanceId);
    }
}
