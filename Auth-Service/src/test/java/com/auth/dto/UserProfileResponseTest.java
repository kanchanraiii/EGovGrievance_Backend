package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileResponseTest {

    @Test
    void builderPopulatesFields() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("u1")
                .email("user@example.com")
                .fullName("User One")
                .phone("123")
                .role("ADMIN")
                .departmentId("D1")
                .build();

        assertThat(profile.getUserId()).isEqualTo("u1");
        assertThat(profile.getEmail()).isEqualTo("user@example.com");
        assertThat(profile.getDepartmentId()).isEqualTo("D1");
    }

    @Test
    void settersAndGettersWithSpy() {
        UserProfileResponse profile = spy(new UserProfileResponse());
        profile.setUserId("u2");
        profile.setEmail("mail");
        profile.setFullName("Full");
        profile.setPhone("999");
        profile.setRole("ROLE");
        profile.setDepartmentId("D2");

        assertThat(profile.getFullName()).isEqualTo("Full");
        assertThat(profile.getPhone()).isEqualTo("999");
        verify(profile).setUserId("u2");
        verify(profile).setDepartmentId("D2");
    }
}
