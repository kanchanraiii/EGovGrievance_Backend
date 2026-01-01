package com.grievance.request;

import com.grievance.model.Grievance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrievanceCreateRequestTest {

    @Test
    void toGrievanceCopiesFields() {
        GrievanceCreateRequest request = new GrievanceCreateRequest();
        request.setCitizenId("citizen");
        request.setDepartmentId("dept");
        request.setCategoryCode("CAT");
        request.setSubCategoryCode("SUB");
        request.setDescription("A detailed description");

        Grievance grievance = request.toGrievance();

        assertThat(grievance.getCitizenId()).isEqualTo("citizen");
        assertThat(grievance.getDepartmentId()).isEqualTo("dept");
        assertThat(grievance.getCategoryCode()).isEqualTo("CAT");
        assertThat(grievance.getSubCategoryCode()).isEqualTo("SUB");
        assertThat(grievance.getDescription()).isEqualTo("A detailed description");
    }
}
