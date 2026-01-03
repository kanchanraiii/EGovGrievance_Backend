package com.grievance.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class CategoryResponseTest {

    @Test
    void gettersAndToString() {
        CategoryResponse category = new CategoryResponse();
        category.setCode("CAT");
        category.setName("Water");
        category.setSubCategories(List.of(new SubCategoryResponse()));

        assertThat(category.getCode()).isEqualTo("CAT");
        assertThat(category.getName()).isEqualTo("Water");
        assertThat(category.getSubCategories()).hasSize(1);
    }
}
