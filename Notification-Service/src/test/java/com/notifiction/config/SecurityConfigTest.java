package com.notifiction.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;


class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void buildsSecurityWebFilterChain() {
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtDecoder(config.jwtDecoder("secret"))));
        assertThat(config.securityWebFilterChain(http)).isNotNull();
    }

    @Test
    void jwtDecoderBuildsWithSecret() {
        ReactiveJwtDecoder decoder = config.jwtDecoder("secret-key");
        assertThat(decoder).isNotNull();
    }
}
