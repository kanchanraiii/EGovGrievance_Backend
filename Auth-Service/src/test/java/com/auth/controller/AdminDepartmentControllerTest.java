package com.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.auth.dto.CategoryRequest;
import com.auth.dto.DepartmentRequest;
import com.auth.service.DepartmentConfigService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDepartmentControllerTest {

    @Mock
    private DepartmentConfigService departmentConfigService;

    @InjectMocks
    private AdminDepartmentController controller;

    private DepartmentRequest departmentRequest;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        departmentRequest = new DepartmentRequest();
        departmentRequest.setId("dept-1");
        departmentRequest.setName("Department One");
        departmentRequest.setLevel("CENTRAL");

        categoryRequest = new CategoryRequest();
        categoryRequest.setCode("cat-1");
        categoryRequest.setName("Category One");
    }

    @Test
    void addDepartmentDelegatesToService() {
        when(departmentConfigService.addDepartment(departmentRequest)).thenReturn(Mono.empty());

        StepVerifier.create(controller.addDepartment(departmentRequest))
                .verifyComplete();

        verify(departmentConfigService).addDepartment(departmentRequest);
    }

    @Test
    void deleteDepartmentDelegatesToService() {
        when(departmentConfigService.deleteDepartment("dept-1")).thenReturn(Mono.empty());

        StepVerifier.create(controller.deleteDepartment("dept-1"))
                .verifyComplete();

        verify(departmentConfigService).deleteDepartment("dept-1");
    }

    @Test
    void addCategoryDelegatesToService() {
        when(departmentConfigService.addCategory("dept-1", categoryRequest)).thenReturn(Mono.empty());

        StepVerifier.create(controller.addCategory("dept-1", categoryRequest))
                .verifyComplete();

        verify(departmentConfigService).addCategory("dept-1", categoryRequest);
    }

    @Test
    void deleteCategoryDelegatesToService() {
        when(departmentConfigService.deleteCategory("dept-1", "cat-1")).thenReturn(Mono.empty());

        StepVerifier.create(controller.deleteCategory("dept-1", "cat-1"))
                .verifyComplete();

        verify(departmentConfigService).deleteCategory("dept-1", "cat-1");
    }
}
