package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.grievance.model.GrievanceStatus;

class StatusUpdateRequestTest {

    @Test
    void gettersEqualsAndHashCodeCoverBranches() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setGrievanceId("g1");
        request.setStatus(GrievanceStatus.ASSIGNED);
        request.setUpdatedBy("officer-1");
        request.setRemarks("in review");

        assertThat(request.getGrievanceId()).isEqualTo("g1");
        assertThat(request.getStatus()).isEqualTo(GrievanceStatus.ASSIGNED);
        assertThat(request.getUpdatedBy()).isEqualTo("officer-1");
        assertThat(request.getRemarks()).isEqualTo("in review");
        assertThat(request).isEqualTo(request);
        assertThat(request).isNotEqualTo("other");

        StatusUpdateRequest same = new StatusUpdateRequest();
        same.setGrievanceId("g1");
        same.setStatus(GrievanceStatus.ASSIGNED);
        same.setUpdatedBy("officer-1");
        same.setRemarks("in review");

        assertThat(request).isEqualTo(same);
        assertThat(request.hashCode()).isEqualTo(same.hashCode());

        StatusUpdateRequest differentStatus = new StatusUpdateRequest();
        differentStatus.setGrievanceId("g1");
        differentStatus.setStatus(GrievanceStatus.RESOLVED);
        differentStatus.setUpdatedBy("officer-1");
        differentStatus.setRemarks("resolved");

        assertThat(request).isNotEqualTo(differentStatus);

        StatusUpdateRequest nullFields = new StatusUpdateRequest();
        assertThat(nullFields.hashCode()).isNotZero();
        assertThat(nullFields).isNotEqualTo(request);

        StatusUpdateRequest refusingEquality = new StatusUpdateRequest() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusingEquality.setGrievanceId("g1");
        refusingEquality.setStatus(GrievanceStatus.ASSIGNED);
        refusingEquality.setUpdatedBy("officer-1");

        assertThat(request.equals(refusingEquality)).isFalse();
    }
}
