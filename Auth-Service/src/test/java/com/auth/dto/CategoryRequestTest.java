package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class CategoryRequestTest {

    @Test
    void gettersAndSettersWork() {
        SubCategoryRequest sub1 = new SubCategoryRequest();
        sub1.setCode("sub-1");
        sub1.setName("Sub One");

        SubCategoryRequest sub2 = new SubCategoryRequest();
        sub2.setCode("sub-2");
        sub2.setName("Sub Two");

        CategoryRequest request = new CategoryRequest();
        request.setCode("cat-1");
        request.setName("Category One");
        request.setSubCategories(List.of(sub1, sub2));

        assertThat(request.getCode()).isEqualTo("cat-1");
        assertThat(request.getName()).isEqualTo("Category One");
        assertThat(request.getSubCategories()).containsExactly(sub1, sub2);
    }
}
