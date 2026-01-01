package com.feedback.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalErrorHandlerExtraTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void handleGenericReturns500() {
        var response = handler.handleGeneric(new Exception("oops"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "Internal server error");
    }

    @Test
    void handleNotFoundReturns404() {
        RuntimeException ex = new RuntimeException("other error");
        var response = handler.handleNotFound(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "other error");
    }
}
