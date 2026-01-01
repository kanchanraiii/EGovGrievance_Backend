package com.grievance.service;

import com.grievance.client.DepartmentClient;
import com.grievance.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentValidationServiceTest {

    @Mock
    private DepartmentClient departmentClient;

    private DepartmentValidationService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentValidationService(departmentClient);
    }

    @Test
    void validateDepartmentCompletesWhenValid() {
        when(departmentClient.isValidDepartment("D1", "C1", "S1")).thenReturn(Mono.just(true));

        StepVerifier.create(service.validateDepartment("D1", "C1", "S1"))
                .verifyComplete();
    }

    @Test
    void validateDepartmentErrorsWhenInvalid() {
        when(departmentClient.isValidDepartment("D1", "C1", "S1")).thenReturn(Mono.just(false));

        StepVerifier.create(service.validateDepartment("D1", "C1", "S1"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessage("Department / category / sub-category not found"))
                .verify();
    }
}
