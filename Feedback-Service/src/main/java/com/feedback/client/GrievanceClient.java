package com.feedback.client;

import org.springframework.web.reactive.function.client.WebClient;
import com.feedback.exception.ResourceNotFoundException;
import com.feedback.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

public class GrievanceClient {
	
	// to connect to grievance service first
	private final WebClient webClient;
	
	public GrievanceClient(
            WebClient.Builder builder,
            @Value("${grievance.service.url}") String grievanceServiceUrl) {

        this.webClient = builder.baseUrl(grievanceServiceUrl).build();
    }
	
	// to get grievance by id
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
	
	// to check if a grievance is resolved or not
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
	
	

}
