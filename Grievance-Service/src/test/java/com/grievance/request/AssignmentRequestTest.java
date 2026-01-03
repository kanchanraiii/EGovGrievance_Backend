package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssignmentRequestTest {

    @Test
    void gettersEqualsAndHashCodeCoverBranches() {
        AssignmentRequest request = new AssignmentRequest();
        request.setGrievanceId("g1");
        request.setAssignedTo("worker-1");

        assertThat(request.getGrievanceId()).isEqualTo("g1");
        assertThat(request.getAssignedTo()).isEqualTo("worker-1");
        assertThat(request).isEqualTo(request);
        assertThat(request).isNotEqualTo("other");

        AssignmentRequest same = new AssignmentRequest();
        same.setGrievanceId("g1");
        same.setAssignedTo("worker-1");

        assertThat(request).isEqualTo(same);
        assertThat(request.hashCode()).isEqualTo(same.hashCode());

        AssignmentRequest different = new AssignmentRequest();
        different.setGrievanceId("g1");
        different.setAssignedTo("worker-2");

        assertThat(request).isNotEqualTo(different);
        assertThat(request.hashCode()).isNotEqualTo(different.hashCode());

        AssignmentRequest missingAssignee = new AssignmentRequest();
        missingAssignee.setGrievanceId("g1");
        missingAssignee.setAssignedTo(null);
        assertThat(request).isNotEqualTo(missingAssignee);

        AssignmentRequest empty1 = new AssignmentRequest();
        AssignmentRequest empty2 = new AssignmentRequest();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(request.equals(null)).isFalse();

        AssignmentRequest nullFields = new AssignmentRequest();
        assertThat(nullFields.hashCode()).isNotZero();
        assertThat(nullFields).isNotEqualTo(request);

        AssignmentRequest refusingEquality = new AssignmentRequest() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setGrievanceId("g1");
        refusingEquality.setAssignedTo("worker-1");

        assertThat(request.equals(refusingEquality)).isFalse();
        assertThat(request.toString()).contains("grievanceId");

        AssignmentRequest missingId = new AssignmentRequest();
        missingId.setAssignedTo("worker-1");
        assertThat(missingId).isNotEqualTo(request);
    }
}
