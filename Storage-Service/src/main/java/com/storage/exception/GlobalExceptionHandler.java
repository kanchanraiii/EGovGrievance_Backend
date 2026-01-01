package com.storage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StorageException.class)
    public Mono<Map<String, Object>> handle(StorageException ex) {
        return Mono.just(
                Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", ex.getMessage()
                )
        );
    }
}
