package com.grievance.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient {

    private final WebClient webClient;
    private final String profilePath;

    public AuthClient(
            WebClient.Builder builder,
            @Value("${auth.service.base-url:http://localhost:9007}") String baseUrl,
            @Value("${auth.service.profile-path:/api/auth/profile}") String profilePath) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.profilePath = profilePath;
    }

    public Mono<String> fetchEmail(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return Mono.empty();
        }
        return webClient
                .get()
                .uri(profilePath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> Mono.empty())
                .bodyToMono(AuthProfileResponse.class)
                .map(AuthProfileResponse::getEmail)
                .filter(email -> email != null && !email.isBlank());
    }
}
