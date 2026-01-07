package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grievance.model.GrievanceStatus;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S5838")
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

        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);
        assertThat(r1.toString()).contains("GrievanceCreateRequest", "description");

        r2.setSubCategoryCode("DIFF");
        assertThat(r1).isNotEqualTo(r2)
                .isEqualTo(r1)
                .isNotEqualTo(null);
        assertThat(r1.canEqual(new Object())).isFalse();

        GrievanceCreateRequest empty1 = new GrievanceCreateRequest();
        GrievanceCreateRequest empty2 = new GrievanceCreateRequest();
        assertThat(empty1).isEqualTo(empty2).hasSameHashCodeAs(empty2);

        GrievanceCreateRequest missingDepartment = new GrievanceCreateRequest();
        missingDepartment.setCategoryCode("CAT");
        missingDepartment.setSubCategoryCode("SUB");
        missingDepartment.setDescription("description");
        GrievanceCreateRequest missingCategory = new GrievanceCreateRequest();
        missingCategory.setDepartmentId("D1");
        missingCategory.setSubCategoryCode("SUB");
        missingCategory.setDescription("description");
        GrievanceCreateRequest missingSubCategory = new GrievanceCreateRequest();
        missingSubCategory.setDepartmentId("D1");
        missingSubCategory.setCategoryCode("CAT");
        missingSubCategory.setDescription("description");
        GrievanceCreateRequest missingDescription = new GrievanceCreateRequest();
        missingDescription.setDepartmentId("D1");
        missingDescription.setCategoryCode("CAT");
        missingDescription.setSubCategoryCode("SUB");
        assertThat(r1).isNotEqualTo(missingDepartment)
                .isNotEqualTo(missingCategory)
                .isNotEqualTo(missingSubCategory)
                .isNotEqualTo(missingDescription);

        GrievanceCreateRequest nullDepartmentBase = new GrievanceCreateRequest();
        nullDepartmentBase.setCategoryCode("CAT");
        nullDepartmentBase.setSubCategoryCode("SUB");
        nullDepartmentBase.setDescription("description");
        assertThat(nullDepartmentBase).isNotEqualTo(r1);

        GrievanceCreateRequest nullCategoryBase = new GrievanceCreateRequest();
        nullCategoryBase.setDepartmentId("D1");
        nullCategoryBase.setSubCategoryCode("SUB");
        nullCategoryBase.setDescription("description");
        assertThat(nullCategoryBase).isNotEqualTo(r1);

        GrievanceCreateRequest nullSubCategoryBase = new GrievanceCreateRequest();
        nullSubCategoryBase.setDepartmentId("D1");
        nullSubCategoryBase.setCategoryCode("CAT");
        nullSubCategoryBase.setDescription("description");
        assertThat(nullSubCategoryBase).isNotEqualTo(r1);

        GrievanceCreateRequest nullDescriptionBase = new GrievanceCreateRequest();
        nullDescriptionBase.setDepartmentId("D1");
        nullDescriptionBase.setCategoryCode("CAT");
        nullDescriptionBase.setSubCategoryCode("SUB");
        assertThat(nullDescriptionBase).isNotEqualTo(r1);

        GrievanceCreateRequest refusingEquality = new GrievanceCreateRequest() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setDepartmentId("D1");
        refusingEquality.setCategoryCode("CAT");
        refusingEquality.setSubCategoryCode("SUB");
        refusingEquality.setDescription("description");
        assertThat(r1.equals(refusingEquality)).isFalse();

        assertThat(r1.equals("other")).isFalse();
    }

    @Test
    void assignmentRequestEqualityAndHashCode() {
        AssignmentRequest a1 = new AssignmentRequest();
        a1.setGrievanceId("g1");
        a1.setAssignedTo("worker");

        AssignmentRequest a2 = new AssignmentRequest();
        a2.setGrievanceId("g1");
        a2.setAssignedTo("worker");

        assertThat(a1).isEqualTo(a2).hasSameHashCodeAs(a2);

        a2.setAssignedTo("other");
        assertThat(a1).isNotEqualTo(a2)
                .isEqualTo(a1)
                .isNotEqualTo(null);
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

        assertThat(s1).isEqualTo(s2).hasSameHashCodeAs(s2);

        s2.setRemarks("done");
        assertThat(s1).isNotEqualTo(s2)
                .isEqualTo(s1)
                .isNotEqualTo(null);
        assertThat(s1.canEqual(new Object())).isFalse();
    }
}
