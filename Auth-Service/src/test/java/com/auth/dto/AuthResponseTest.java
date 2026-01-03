package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthResponseTest {

    @Test
    void builderPopulatesAllFields() {
        Instant expires = Instant.now().plusSeconds(3600);

        AuthResponse response = AuthResponse.builder()
                .token("token-1")
                .expiresAt(expires)
                .userId("u1")
                .email("user@example.com")
                .fullName("User One")
                .phone("12345")
                .role("ADMIN")
                .departmentId("D1")
                .build();

        assertThat(response.getToken()).isEqualTo("token-1");
        assertThat(response.getExpiresAt()).isEqualTo(expires);
        assertThat(response.getUserId()).isEqualTo("u1");
        assertThat(response.getDepartmentId()).isEqualTo("D1");
    }

    @Test
    void settersAndGettersWorkWithSpy() {
        AuthResponse response = spy(new AuthResponse());
        response.setToken("t2");
        response.setExpiresAt(Instant.EPOCH);
        response.setUserId("u2");
        response.setEmail("mail");
        response.setFullName("Full Name");
        response.setPhone("999");
        response.setRole("CITIZEN");
        response.setDepartmentId("D2");

        assertThat(response.getToken()).isEqualTo("t2");
        assertThat(response.getExpiresAt()).isEqualTo(Instant.EPOCH);
        assertThat(response.getRole()).isEqualTo("CITIZEN");
        verify(response).setToken("t2");
        verify(response).setDepartmentId("D2");
    }
}
