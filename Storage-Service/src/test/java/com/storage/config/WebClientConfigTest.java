package com.storage.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WebClientConfigTest {

    private final WebClientConfig config = new WebClientConfig();

    @Test
    void addsAuthorizationHeaderWhenJwtPresent() {
        AtomicReference<String> capturedAuth = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedAuth.set(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient client = config.webClientBuilder()
                .exchangeFunction(exchangeFunction)
                .build();

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("role", "DEPARTMENT_OFFICER").build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        SecurityContextImpl context = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        client.get()
                                .uri("http://example.com")
                                .retrieve()
                                .bodyToMono(Void.class)
                                .contextWrite(org.springframework.security.core.context.ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
                )
                .verifyComplete();

        assertThat(capturedAuth.get()).isEqualTo("Bearer token");
    }

    @Test
    void leavesRequestUnchangedWhenNoSecurityContext() {
        AtomicReference<String> capturedAuth = new AtomicReference<>("init");
        ExchangeFunction exchangeFunction = request -> {
            capturedAuth.set(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient client = config.webClientBuilder()
                .exchangeFunction(exchangeFunction)
                .build();

        StepVerifier.create(client.get().uri("http://example.com").retrieve().bodyToMono(Void.class))
                .verifyComplete();

        assertThat(capturedAuth.get()).isNull();
    }
}
