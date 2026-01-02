package com.auth.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import com.auth.model.User;

import reactor.core.publisher.Mono;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final Duration tokenTtl;
    private final String issuer;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.expiration-minutes:60}") long expirationMinutes,
            @Value("${spring.application.name:auth-service}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.tokenTtl = Duration.ofMinutes(expirationMinutes);
        this.issuer = issuer;
    }

    public Instant buildExpiry() {
        return Instant.now().plus(tokenTtl);
    }

    public Mono<String> generateToken(User user, Instant expiresAt) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("phone", user.getPhone())
                .claim("role", user.getRole())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return Mono.fromCallable(() -> jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue());
    }
}
