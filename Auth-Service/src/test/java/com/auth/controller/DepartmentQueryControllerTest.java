package com.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.auth.service.DepartmentConfigService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepartmentQueryControllerTest {

    @Mock
    private DepartmentConfigService departmentConfigService;

    @InjectMocks
    private DepartmentQueryController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void getAllDepartmentsDelegatesToService() {
        ObjectNode root = mapper.createObjectNode();
        when(departmentConfigService.getAllDepartments()).thenReturn(Mono.just(root));

        StepVerifier.create(controller.getAllDepartments())
                .expectNext(root)
                .verifyComplete();

        verify(departmentConfigService).getAllDepartments();
    }

    @Test
    void getCategoriesDelegatesToService() {
        ArrayNode categories = mapper.createArrayNode();
        when(departmentConfigService.getCategories("dept-1")).thenReturn(Mono.just(categories));

        StepVerifier.create(controller.getCategories("dept-1"))
                .expectNext(categories)
                .verifyComplete();

        verify(departmentConfigService).getCategories("dept-1");
    }
}
