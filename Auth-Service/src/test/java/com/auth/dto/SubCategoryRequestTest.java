package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubCategoryRequestTest {

    @Test
    void gettersAndSettersWork() {
        SubCategoryRequest request = new SubCategoryRequest();
        request.setCode("sub-1");
        request.setName("Sub One");

        assertThat(request.getCode()).isEqualTo("sub-1");
        assertThat(request.getName()).isEqualTo("Sub One");
    }
}
