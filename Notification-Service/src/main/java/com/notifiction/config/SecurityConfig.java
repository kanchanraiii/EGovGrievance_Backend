package com.notifiction.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/v3/api-docs/swagger-config", "/webjars/**", "/swagger-resources/**",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
