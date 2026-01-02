package com.feedback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(authorizationPropagationFilter());
    }

    private ExchangeFilterFunction authorizationPropagationFilter() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .map(JwtAuthenticationToken.class::cast)
                .map(auth -> ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getToken().getTokenValue())
                        .build())
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }
}
