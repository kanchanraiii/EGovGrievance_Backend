package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grievance.model.GrievanceStatus;

@ExtendWith(MockitoExtension.class)
class RequestEqualityTest {

    @Test
    void grievanceCreateRequestEqualityAndHashCode() {
        GrievanceCreateRequest r1 = new GrievanceCreateRequest();
        r1.setDepartmentId("D1");
        r1.setCategoryCode("CAT");
        r1.setSubCategoryCode("SUB");
        r1.setDescription("description");

        GrievanceCreateRequest r2 = new GrievanceCreateRequest();
        r2.setDepartmentId("D1");
        r2.setCategoryCode("CAT");
        r2.setSubCategoryCode("SUB");
        r2.setDescription("description");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        assertThat(r1.toString()).contains("GrievanceCreateRequest").contains("description");

        r2.setSubCategoryCode("DIFF");
        assertThat(r1).isNotEqualTo(r2);

        assertThat(r1).isEqualTo(r1);
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1.canEqual(new Object())).isFalse();
    }

    @Test
    void assignmentRequestEqualityAndHashCode() {
        AssignmentRequest a1 = new AssignmentRequest();
        a1.setGrievanceId("g1");
        a1.setAssignedTo("worker");

        AssignmentRequest a2 = new AssignmentRequest();
        a2.setGrievanceId("g1");
        a2.setAssignedTo("worker");

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

        a2.setAssignedTo("other");
        assertThat(a1).isNotEqualTo(a2);

        assertThat(a1).isEqualTo(a1);
        assertThat(a1).isNotEqualTo(null);
        assertThat(a1.canEqual(new Object())).isFalse();
    }

    @Test
    void statusUpdateRequestEqualityAndHashCode() {
        StatusUpdateRequest s1 = new StatusUpdateRequest();
        s1.setGrievanceId("g1");
        s1.setStatus(GrievanceStatus.IN_PROGRESS);
        s1.setUpdatedBy("worker");
        s1.setRemarks("working");

        StatusUpdateRequest s2 = new StatusUpdateRequest();
        s2.setGrievanceId("g1");
        s2.setStatus(GrievanceStatus.IN_PROGRESS);
        s2.setUpdatedBy("worker");
        s2.setRemarks("working");

        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());

        s2.setRemarks("done");
        assertThat(s1).isNotEqualTo(s2);

        assertThat(s1).isEqualTo(s1);
        assertThat(s1).isNotEqualTo(null);
        assertThat(s1.canEqual(new Object())).isFalse();
    }
}
