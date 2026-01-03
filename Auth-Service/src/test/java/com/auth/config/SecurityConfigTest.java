package com.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void buildsSecurityWebFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(securityConfig.jwtDecoder("secret-value"))));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);

        assertThat(chain).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterUsesRoleClaim() {
        Converter<Jwt, Mono<AbstractAuthenticationToken>> converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("sub", "user-1")
                .claim("role", "ADMIN")
                .claim("exp", Instant.now())
                .build();

        StepVerifier.create(converter.convert(jwt))
                .assertNext(auth -> assertThat(auth.getAuthorities())
                        .extracting(GrantedAuthority::getAuthority)
                        .contains("ROLE_ADMIN"))
                .verifyComplete();
    }

    @Test
    void jwtEncoderAndDecoderAreCreatedFromSecret() {
        assertThat(securityConfig.jwtEncoder("secret-value")).isNotNull();
        assertThat(securityConfig.jwtDecoder("secret-value")).isNotNull();
    }

    @Test
    void passwordEncoderIsDelegating() {
        var encoder = securityConfig.passwordEncoder();
        String encoded = encoder.encode("password");

        assertThat(encoder.matches("password", encoded)).isTrue();
    }
}
