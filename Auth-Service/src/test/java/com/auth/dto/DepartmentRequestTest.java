package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DepartmentRequestTest {

    @Test
    void gettersAndSettersWork() {
        CategoryRequest category = new CategoryRequest();
        category.setCode("cat-1");
        category.setName("Category One");

        DepartmentRequest request = new DepartmentRequest();
        request.setId("dept-1");
        request.setName("Department One");
        request.setLevel("central");
        request.setCategories(List.of(category));

        assertThat(request.getId()).isEqualTo("dept-1");
        assertThat(request.getName()).isEqualTo("Department One");
        assertThat(request.getLevel()).isEqualTo("central");
        assertThat(request.getCategories()).containsExactly(category);
    }
}
