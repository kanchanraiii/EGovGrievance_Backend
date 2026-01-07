package com.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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

        Mono<AbstractAuthenticationToken> result = securityConfig.jwtAuthenticationConverter().convert(jwt);

        StepVerifier.create(result)
                .assertNext(auth -> assertThat(auth.getAuthorities())
                        .anySatisfy(granted -> assertThat(granted.getAuthority()).isEqualTo("ROLE_ADMIN")))
                .verifyComplete();
    }

    @Test
    void securityWebFilterChainBuildsSuccessfully() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        ReactiveJwtDecoder decoder = token -> Mono.error(new IllegalStateException("noop"));
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(decoder)));
        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        assertThat(chain).isNotNull();
    }

    @Test
    void jwtDecoderBuildsAndValidatesHs256Token() throws Exception {
        String secret = "01234567890123456789012345678901";

        ReactiveJwtDecoder decoder = securityConfig.jwtDecoder(secret);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("user-123")
                .claim("role", "CITIZEN")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJWT.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));

        StepVerifier.create(decoder.decode(signedJWT.serialize()))
                .assertNext(jwt -> {
                    assertThat(jwt.getSubject()).isEqualTo("user-123");
                    assertThat(jwt.getClaimAsString("role")).isEqualTo("CITIZEN");
                })
                .verifyComplete();
    }
}
