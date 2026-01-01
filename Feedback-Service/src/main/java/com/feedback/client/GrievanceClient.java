package com.feedback.client;

import org.springframework.web.reactive.function.client.WebClient;
import com.feedback.exception.ResourceNotFoundException;
import com.feedback.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;

@Component
public class GrievanceClient {
	
	private final WebClient webClient;

	public GrievanceClient(
            WebClient.Builder builder,
            @Value("${grievance.service.url}") String grievanceServiceUrl) {

        this.webClient = builder.baseUrl(grievanceServiceUrl).build();
    }
	
	public Mono<GrievanceResponse> getGrievanceById(String grievanceId) {
        return webClient
                .get()
                .uri("/api/grievances/{id}", grievanceId)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> Mono.error(
                                new ResourceNotFoundException("Grievance not found"))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(
                                new ServiceException("Grievance service unavailable"))
                )
                .bodyToMono(GrievanceResponse.class);
    }
	
	public Mono<Void> validateResolvedGrievance(String grievanceId) {
        return getGrievanceById(grievanceId)
                .flatMap(grievance -> {
                    if (!"RESOLVED".equals(grievance.getStatus())) {
                        return Mono.error(
                                new ServiceException("Feedback allowed only for RESOLVED"));
                    }
                    return Mono.empty();
                });
    }
	
	
    public Mono<Void> updateStatus(String grievanceId, String status, String updatedBy, String remarks) {
        StatusUpdatePayload payload = new StatusUpdatePayload(grievanceId, status, updatedBy, remarks);

        return webClient
                .patch()
                .uri("/api/grievances/status")
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.is4xxClientError(),
                        response -> Mono.error(new ResourceNotFoundException("Grievance not found"))
                )
                .onStatus(
                        httpStatus -> httpStatus.is5xxServerError(),
                        response -> Mono.error(new ServiceException("Grievance service unavailable"))
                )
                .bodyToMono(Void.class);
    }

    public Mono<Void> markAsReopened(String grievanceId, String remarks) {
        return updateStatus(grievanceId, "REOPENED", "CITIZEN_REOPEN", remarks);
    }

    private record StatusUpdatePayload(
            String grievanceId,
            String status,
            String updatedBy,
            String remarks
    ) { }


}
