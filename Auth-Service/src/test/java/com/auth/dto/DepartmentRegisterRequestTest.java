package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepartmentRegisterRequestTest {

    @Test
    void settersAndGettersWorkWithSpy() {
        DepartmentRegisterRequest request = spy(new DepartmentRegisterRequest());
        request.setFullName("Dept User");
        request.setEmail("dept@example.com");
        request.setPhone("1234567890");
        request.setPassword("password1");
        request.setDepartmentId("D1");

        assertThat(request.getFullName()).isEqualTo("Dept User");
        assertThat(request.getDepartmentId()).isEqualTo("D1");
        verify(request).setEmail("dept@example.com");
        verify(request).setDepartmentId("D1");
    }
}
