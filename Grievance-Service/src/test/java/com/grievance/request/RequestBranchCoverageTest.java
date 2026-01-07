package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.grievance.model.GrievanceStatus;

@SuppressWarnings("java:S5838")
class RequestBranchCoverageTest {

    @Test
    void assignmentRequestEqualityBranches() {
        AssignmentRequest r1 = new AssignmentRequest();
        r1.setGrievanceId("g1");
        r1.setAssignedTo("cw1");

        assertThat(r1).isEqualTo(r1)
                .isNotEqualTo(null)
                .isNotEqualTo("not-a-request");

        AssignmentRequest r2 = new AssignmentRequest();
        r2.setGrievanceId("g1");
        r2.setAssignedTo("cw1");

        AssignmentRequest r3 = new AssignmentRequest();
        r3.setGrievanceId("g2");
        r3.setAssignedTo("cw2");

        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2)
                .isNotEqualTo(r3);
    }

    @Test
    void statusUpdateRequestEqualityBranches() {
        StatusUpdateRequest s1 = new StatusUpdateRequest();
        s1.setGrievanceId("g1");
        s1.setStatus(GrievanceStatus.ASSIGNED);
        s1.setUpdatedBy("user1");
        s1.setRemarks("rem");

        assertThat(s1).isEqualTo(s1)
                .isNotEqualTo(null)
                .isNotEqualTo("other");

        StatusUpdateRequest s2 = new StatusUpdateRequest();
        s2.setGrievanceId("g1");
        s2.setStatus(GrievanceStatus.ASSIGNED);
        s2.setUpdatedBy("user1");
        s2.setRemarks("rem");

        StatusUpdateRequest s3 = new StatusUpdateRequest();
        s3.setGrievanceId("g2");
        s3.setStatus(GrievanceStatus.CLOSED);
        s3.setUpdatedBy("user2");
        s3.setRemarks("different");

        assertThat(s1).isEqualTo(s2).hasSameHashCodeAs(s2)
                .isNotEqualTo(s3);
    }

    @Test
    void assignmentRequestCanEqualBranch() {
        AssignmentRequest base = new AssignmentRequest();
        base.setGrievanceId("g1");
        base.setAssignedTo("cw1");

        AssignmentRequestChild child = new AssignmentRequestChild();
        child.setGrievanceId("g1");
        child.setAssignedTo("cw1");

        assertThat(base.equals(child)).isFalse();
    }

    @Test
    void statusUpdateRequestCanEqualBranch() {
        StatusUpdateRequest base = new StatusUpdateRequest();
        base.setGrievanceId("g1");
        base.setStatus(GrievanceStatus.ASSIGNED);
        base.setUpdatedBy("user1");

        StatusUpdateRequestChild child = new StatusUpdateRequestChild();
        child.setGrievanceId("g1");
        child.setStatus(GrievanceStatus.ASSIGNED);
        child.setUpdatedBy("user1");

        assertThat(base.equals(child)).isFalse();
    }

    private static class AssignmentRequestChild extends AssignmentRequest {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }

    private static class StatusUpdateRequestChild extends StatusUpdateRequest {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }
}
