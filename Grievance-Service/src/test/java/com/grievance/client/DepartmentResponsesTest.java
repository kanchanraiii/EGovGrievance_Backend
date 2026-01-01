package com.grievance.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentResponsesTest {

    @Test
    void gettersAndSettersWorkForResponses() {
        SubCategoryResponse sub = new SubCategoryResponse();
        sub.setCode("S1");
        sub.setName("Sub Name");

        CategoryResponse category = new CategoryResponse();
        category.setCode("C1");
        category.setName("Category Name");
        category.setSubCategories(List.of(sub));

        DepartmentResponse department = new DepartmentResponse();
        department.setId("D1");
        department.setName("Water");
        department.setLevel("Central");
        department.setCategories(List.of(category));

        assertThat(department.getId()).isEqualTo("D1");
        assertThat(department.getCategories()).hasSize(1);
        assertThat(department.getCategories().get(0).getSubCategories().get(0).getName())
                .isEqualTo("Sub Name");
    }
}
