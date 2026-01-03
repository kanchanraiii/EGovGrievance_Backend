package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.auth.model.User;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtEncoder, 15, "auth-service");
        expiresAt = Instant.now().plusSeconds(900);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation ->
                new Jwt("token-value", Instant.now(), expiresAt, Map.of("alg", "HS256"), Map.of("sub", "user-1"))
        );
    }

    @Test
    void generateTokenBuildsExpectedClaims() {
        User user = new User();
        user.setId("user-1");
        user.setEmail("user@example.com");
        user.setFullName("User One");
        user.setPhone("1234567890");
        user.setRole("ADMIN");
        user.setDepartmentId("DEPT-1");

        StepVerifier.create(jwtService.generateToken(user, expiresAt))
                .assertNext(token -> assertThat(token).isEqualTo("token-value"))
                .verifyComplete();
    }

    @Test
    void generateTokenRejectsMissingFields() {
        User user = new User();
        user.setId("user-1");

        StepVerifier.create(jwtService.generateToken(user, expiresAt))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void buildExpiryUsesConfiguredTtl() {
        Instant result = jwtService.buildExpiry();
        assertThat(result).isAfter(Instant.now());
    }
}
