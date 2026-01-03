package com.grievance.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtAuthenticationConverterAddsRolePrefix() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("role", "ADMIN")
                .build();

        Mono<AbstractAuthenticationToken> converted = securityConfig.jwtAuthenticationConverter().convert(jwt);

        StepVerifier.create(converted)
                .assertNext(auth -> assertThat(auth.getAuthorities())
                        .anySatisfy(a -> assertThat(a.getAuthority()).isEqualTo("ROLE_ADMIN")))
                .verifyComplete();
    }

    @Test
    void securityWebFilterChainBuildsWithDecoder() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        ReactiveJwtDecoder decoder = token -> Mono.error(new IllegalStateException("noop"));
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder)));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        assertThat(chain).isNotNull();
    }
}
