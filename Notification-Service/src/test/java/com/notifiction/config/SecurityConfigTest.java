package com.notifiction.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void buildsSecurityWebFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(token -> Mono.empty())));
        assertThat(config.securityWebFilterChain(http)).isNotNull();
    }

    @Test
    void jwtDecoderBuildsWithSecret() {
        ReactiveJwtDecoder decoder = config.jwtDecoder("secret-key");
        assertThat(decoder).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterUsesRoleClaim() {
        Converter<Jwt, Mono<AbstractAuthenticationToken>> converter = config.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("role", "ADMIN")
                .build();

        StepVerifier.create(converter.convert(jwt))
                .assertNext(auth -> {
                    assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
                })
                .verifyComplete();
    }
}
