package com.storage.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.storage.exception.ResourceNotFoundException;
import com.storage.exception.ServiceException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GrievanceClientTest {

    @Test
    void validateGrievanceCompletesOnSuccess() {
        ExchangeFunction exchangeFunction = request ->
                Mono.just(ClientResponse.create(HttpStatus.OK).build());
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(exchangeFunction), "http://example.com");

        StepVerifier.create(client.validateGrievance("123"))
                .verifyComplete();
    }

    @Test
    void validateGrievanceMaps404ToResourceNotFound() {
        ExchangeFunction exchangeFunction = request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(exchangeFunction), "http://example.com");

        StepVerifier.create(client.validateGrievance("missing"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void validateGrievanceMaps500ToServiceException() {
        ExchangeFunction exchangeFunction = request ->
                Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(exchangeFunction), "http://example.com");

        StepVerifier.create(client.validateGrievance("missing"))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void requestFailuresAreWrapped() {
        WebClientRequestException requestException = new WebClientRequestException(
                new IOException("boom"),
                HttpMethod.GET,
                URI.create("http://example.com"),
                new HttpHeaders());
        ExchangeFunction exchangeFunction = request -> Mono.error(requestException);
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(exchangeFunction), "http://example.com");

        StepVerifier.create(client.validateGrievance("id"))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(ServiceException.class))
                .verify();
    }
}
