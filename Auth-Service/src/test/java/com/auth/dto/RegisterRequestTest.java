package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterRequestTest {

    @Test
    void builderPopulatesFields() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Full Name")
                .email("mail@example.com")
                .phone("1234567890")
                .password("password1")
                .departmentId("D1")
                .build();

        assertThat(request.getFullName()).isEqualTo("Full Name");
        assertThat(request.getEmail()).isEqualTo("mail@example.com");
        assertThat(request.getDepartmentId()).isEqualTo("D1");
    }

    @Test
    void settersWithSpy() {
        RegisterRequest request = spy(new RegisterRequest());
        request.setFullName("Name");
        request.setEmail("e");
        request.setPhone("p");
        request.setPassword("pwd");
        request.setDepartmentId("D2");

        assertThat(request.getPassword()).isEqualTo("pwd");
        assertThat(request.getDepartmentId()).isEqualTo("D2");
        verify(request).setFullName("Name");
        verify(request).setDepartmentId("D2");
    }
}
