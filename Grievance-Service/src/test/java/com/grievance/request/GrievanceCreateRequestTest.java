package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grievance.model.Grievance;

@ExtendWith(MockitoExtension.class)
class GrievanceCreateRequestTest {

    @Test
    void toGrievanceCopiesFields() {
        GrievanceCreateRequest request = spy(new GrievanceCreateRequest());
        request.setDepartmentId("D1");
        request.setCategoryCode("CAT");
        request.setSubCategoryCode("SUB");
        request.setDescription("A valid description of the issue");

        Grievance grievance = request.toGrievance();

        assertThat(grievance.getDepartmentId()).isEqualTo("D1");
        assertThat(grievance.getCategoryCode()).isEqualTo("CAT");
        assertThat(grievance.getSubCategoryCode()).isEqualTo("SUB");
        assertThat(grievance.getDescription()).isEqualTo("A valid description of the issue");
        verify(request).setDepartmentId("D1");
        verify(request).setDescription("A valid description of the issue");
    }
}
