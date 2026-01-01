package com.feedback.client;

import com.feedback.exception.ResourceNotFoundException;
import com.feedback.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

class GrievanceClientTest {

    @Test
    void getGrievanceById_returnsBody() {
        String json = "{\"id\":\"g1\",\"status\":\"RESOLVED\"}";
        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(req -> Mono.just(
                        ClientResponse.create(org.springframework.http.HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(json)
                                .build()
                )),
                "http://localhost"
        );

        StepVerifier.create(client.getGrievanceById("g1"))
                .expectNextMatches(resp -> resp.getId().equals("g1") && resp.getStatus().equals("RESOLVED"))
                .verifyComplete();
    }

    @Test
    void getGrievanceById_404ThrowsResourceNotFound() {
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(req -> Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.NOT_FOUND).build()
        )), "http://localhost");

        StepVerifier.create(client.getGrievanceById("missing"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getGrievanceById_500ThrowsServiceException() {
        GrievanceClient client = new GrievanceClient(WebClient.builder().exchangeFunction(req -> Mono.just(
                ClientResponse.create(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build()
        )), "http://localhost");

        StepVerifier.create(client.getGrievanceById("missing"))
                .expectError(ServiceException.class)
                .verify();
    }

    @Test
    void markAsReopened_sendsPatch() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction exchange = request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK).build());
        };

        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(exchange),
                "http://localhost"
        );

        StepVerifier.create(client.markAsReopened("g1", "Reason text"))
                .verifyComplete();

        ClientRequest sent = captured.get();
        org.assertj.core.api.Assertions.assertThat(sent).isNotNull();
        org.assertj.core.api.Assertions.assertThat(sent.method().name()).isEqualTo("PATCH");
        org.assertj.core.api.Assertions.assertThat(sent.url().getPath()).contains("/api/grievances/status");
    }

    @Test
    void validateResolvedGrievance_allowsResolved() {
        String json = "{\"id\":\"g1\",\"status\":\"RESOLVED\"}";
        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(req -> Mono.just(
                        ClientResponse.create(org.springframework.http.HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(json)
                                .build()
                )),
                "http://localhost"
        );

        StepVerifier.create(client.validateResolvedGrievance("g1"))
                .verifyComplete();
    }

    @Test
    void validateResolvedGrievance_rejectsWhenNotResolved() {
        String json = "{\"id\":\"g1\",\"status\":\"IN_PROGRESS\"}";
        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(req -> Mono.just(
                        ClientResponse.create(org.springframework.http.HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(json)
                                .build()
                )),
                "http://localhost"
        );

        StepVerifier.create(client.validateResolvedGrievance("g1"))
                .expectError(ServiceException.class)
                .verify();
    }
}
