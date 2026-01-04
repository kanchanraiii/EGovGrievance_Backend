package com.storage.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Map;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesValidation() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getMessage()).thenReturn("bad");
        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).containsEntry("details", "bad");
    }

    @Test
    void handlesStorageException() {
        ResponseEntity<Map<String, String>> response = handler.handleStorage(new StorageException("oops"));
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("message", "oops");
    }

    @Test
    void handlesNotFound() {
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(new ResourceNotFoundException("missing"));
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("message", "missing");
    }

    @Test
    void handlesServiceUnavailable() {
        ResponseEntity<Map<String, String>> response = handler.handleService(new ServiceException("down"));
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).containsEntry("message", "down");
    }

    @Test
    void handlesIllegalArgument() {
        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(new IllegalArgumentException("illegal"));
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("message", "illegal");
    }

    @Test
    void handlesGeneric() {
        ResponseEntity<Map<String, String>> response = handler.handleGeneric(new RuntimeException("x"));
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }
}
