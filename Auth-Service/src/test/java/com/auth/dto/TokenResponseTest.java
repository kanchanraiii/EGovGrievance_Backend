package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenResponseTest {

    @Test
    void constructorSetsFields() {
        Instant expiry = Instant.now();
        TokenResponse response = new TokenResponse("token", expiry);
        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void settersWorkWithSpy() {
        TokenResponse response = spy(new TokenResponse());
        response.setToken("other");
        response.setExpiresAt(Instant.EPOCH);

        assertThat(response.getToken()).isEqualTo("other");
        assertThat(response.getExpiresAt()).isEqualTo(Instant.EPOCH);
        verify(response).setToken("other");
        verify(response).setExpiresAt(Instant.EPOCH);
    }
}
