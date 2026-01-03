package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthRequestTest {

    @Test
    void constructorSetsFields() {
        AuthRequest request = new AuthRequest("user@mail.com", "secret");
        assertThat(request.getEmail()).isEqualTo("user@mail.com");
        assertThat(request.getPassword()).isEqualTo("secret");
    }

    @Test
    void settersWithSpy() {
        AuthRequest request = spy(new AuthRequest());
        request.setEmail("e");
        request.setPassword("p");

        assertThat(request.getEmail()).isEqualTo("e");
        assertThat(request.getPassword()).isEqualTo("p");
        verify(request).setEmail("e");
        verify(request).setPassword("p");
    }
}
