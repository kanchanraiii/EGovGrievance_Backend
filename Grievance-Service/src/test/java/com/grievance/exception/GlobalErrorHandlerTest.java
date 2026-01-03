package com.grievance.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void handleValidationReturnsBadRequest() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "field", "msg"));
        WebExchangeBindException ex = new WebExchangeBindException(null, bindingResult);

        var response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Invalid input: field");
    }

    @Test
    void handleNotFoundReturns404() {
        ResourceNotFoundException ex = mock(ResourceNotFoundException.class);
        when(ex.getMessage()).thenReturn("missing");
        var response = handler.handleNotFound(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "missing");
    }

    @Test
    void handleServiceReturnsUnavailable() {
        var response = handler.handleService(new ServiceException("down"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleIllegalArgumentReturnsBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad");
    }

    @Test
    void handleGenericReturns500() {
        var response = handler.handleGeneric(new Exception("err"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "Internal server error");
    }

    @Test
    void handleResponseStatusUsesProvidedReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "conflict");
        var response = handler.handleResponseStatus(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "conflict");
    }

    @Test
    void handleResponseStatusFallsBackToMessageWhenReasonMissing() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, null, null);
        var response = handler.handleResponseStatus(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", ex.getMessage());
    }
}
