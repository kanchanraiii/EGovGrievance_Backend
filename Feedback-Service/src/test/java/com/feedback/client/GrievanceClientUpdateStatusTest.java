package com.feedback.client;

import com.feedback.exception.ResourceNotFoundException;
import com.feedback.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GrievanceClientUpdateStatusTest {

    @Test
    void updateStatus_throwsOn404() {
        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(req -> Mono.just(
                        ClientResponse.create(org.springframework.http.HttpStatus.NOT_FOUND).build()
                )),
                "http://localhost"
        );

        StepVerifier.create(client.updateStatus("g1", "REOPENED", "u1", "reason"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void updateStatus_throwsOn500() {
        GrievanceClient client = new GrievanceClient(
                WebClient.builder().exchangeFunction(req -> Mono.just(
                        ClientResponse.create(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build()
                )),
                "http://localhost"
        );

        StepVerifier.create(client.updateStatus("g1", "REOPENED", "u1", "reason"))
                .expectError(ServiceException.class)
                .verify();
    }
}
