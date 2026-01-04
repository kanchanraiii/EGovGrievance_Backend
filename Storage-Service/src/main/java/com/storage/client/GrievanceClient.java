package com.storage.client;

import com.storage.exception.ResourceNotFoundException;
import com.storage.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
public class GrievanceClient {

    private final WebClient webClient;

    public GrievanceClient(
            WebClient.Builder builder,
            @Value("${grievance.service.url}") String grievanceServiceUrl) {

        this.webClient = builder.baseUrl(grievanceServiceUrl).build();
    }

    public Mono<Void> validateGrievance(String grievanceId) {
        return webClient
                .get()
                .uri("/api/grievances/{id}", grievanceId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new ResourceNotFoundException("Grievance not found"))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceException("Grievance service unavailable"))
                )
                .bodyToMono(Void.class)
                .onErrorMap(
                        WebClientRequestException.class,
                        ex -> new ServiceException("Grievance service unavailable")
                )
                .then();
    }
}
