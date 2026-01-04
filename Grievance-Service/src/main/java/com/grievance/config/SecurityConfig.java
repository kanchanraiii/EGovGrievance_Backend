package com.grievance.config;

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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String ROLE_CITIZEN = "CITIZEN";
    private static final String ROLE_DEPARTMENT_OFFICER = "DEPARTMENT_OFFICER";
    private static final String ROLE_SUPERVISORY_OFFICER = "SUPERVISORY_OFFICER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_CASE_WORKER = "CASE_WORKER";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/departments/**").permitAll()
                        .pathMatchers("/api/grievances/create").hasRole(ROLE_CITIZEN)
                        .pathMatchers("/api/grievances/my").hasRole(ROLE_CITIZEN)
                        .pathMatchers("/api/grievances/my-case-workers")
                        .hasAnyRole(ROLE_DEPARTMENT_OFFICER, ROLE_SUPERVISORY_OFFICER, ROLE_ADMIN)
                        .pathMatchers("/api/grievances/history/**")
                        .hasAnyRole(ROLE_CITIZEN, ROLE_CASE_WORKER, ROLE_DEPARTMENT_OFFICER, ROLE_SUPERVISORY_OFFICER, ROLE_ADMIN)
                        .pathMatchers("/api/grievances/getAll", "/api/grievances/assign")
                        .hasAnyRole(ROLE_DEPARTMENT_OFFICER, ROLE_SUPERVISORY_OFFICER, ROLE_ADMIN)
                        .pathMatchers("/api/grievances/status")
                        .hasAnyRole(ROLE_CASE_WORKER, ROLE_DEPARTMENT_OFFICER, ROLE_SUPERVISORY_OFFICER, ROLE_ADMIN)
                        .pathMatchers("/api/grievances/department/**")
                        .hasAnyRole(ROLE_DEPARTMENT_OFFICER, ROLE_CASE_WORKER, ROLE_ADMIN)
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthoritiesClaimName("role");
        delegate.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(delegate);

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
