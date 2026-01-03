package com.grievance.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DepartmentResponseTest {

    @Test
    void gettersCoverAllFields() {
        DepartmentResponse response = new DepartmentResponse();
        response.setId("D1");
        response.setName("Department");
        response.setLevel("STATE");
        response.setCategories(List.of(new CategoryResponse()));

        assertThat(response.getId()).isEqualTo("D1");
        assertThat(response.getName()).isEqualTo("Department");
        assertThat(response.getLevel()).isEqualTo("STATE");
        assertThat(response.getCategories()).hasSize(1);
    }
}
