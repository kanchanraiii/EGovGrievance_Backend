package com.storage.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.test.StepVerifier;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void securityWebFilterChainBuilds() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(token -> reactor.core.publisher.Mono.empty())));
        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        assertThat(chain).isNotNull();
    }

    @Test
    void jwtDecoderCreatesDecoder() {
        ReactiveJwtDecoder decoder = securityConfig.jwtDecoder("super-secret-key-that-is-long-enough");
        assertThat(decoder).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterMapsRole() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("role", "OFFICER").build();

        StepVerifier.create(securityConfig.jwtAuthenticationConverter().convert(jwt))
                .assertNext(auth -> {
                    AbstractAuthenticationToken token = (AbstractAuthenticationToken) auth;
                    assertThat(token.getAuthorities())
                            .extracting("authority")
                            .contains("ROLE_OFFICER");
                })
                .verifyComplete();
    }
}
