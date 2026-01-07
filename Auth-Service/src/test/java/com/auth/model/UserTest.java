package com.auth.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void getterReturnsCreatedAt() {
        Instant now = Instant.now();
        User user = User.builder().createdAt(now).build();

        assertThat(user.getCreatedAt()).isEqualTo(now);
    }
}
