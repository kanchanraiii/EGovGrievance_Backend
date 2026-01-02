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
import org.springframework.util.StringUtils;

import com.auth.model.User;
import com.auth.model.UserRole;

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

        String role = StringUtils.hasText(user.getRole()) ? user.getRole() : UserRole.CITIZEN.value();
        String email = user.getEmail();
        String fullName = user.getFullName();
        String phone = user.getPhone();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(fullName) || !StringUtils.hasText(phone)) {
            return Mono.error(new IllegalArgumentException("Missing required user fields for token"));
        }

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId())
                .claim("email", email)
                .claim("name", fullName)
                .claim("phone", phone)
                .claim("role", role);

        if (user.getDepartmentId() != null) {
            claimsBuilder.claim("departmentId", user.getDepartmentId());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return Mono.fromCallable(() -> jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue());
    }
}
