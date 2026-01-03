package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grievance.model.GrievanceStatus;

@ExtendWith(MockitoExtension.class)
class StatusUpdateRequestTest {

    @Test
    void settersAndGettersWork() {
        StatusUpdateRequest request = spy(new StatusUpdateRequest());
        request.setGrievanceId("g1");
        request.setStatus(GrievanceStatus.IN_PROGRESS);
        request.setUpdatedBy("worker");
        request.setRemarks("working");

        assertThat(request.getGrievanceId()).isEqualTo("g1");
        assertThat(request.getStatus()).isEqualTo(GrievanceStatus.IN_PROGRESS);
        assertThat(request.getUpdatedBy()).isEqualTo("worker");
        assertThat(request.getRemarks()).isEqualTo("working");

        verify(request).setStatus(GrievanceStatus.IN_PROGRESS);
        verify(request).setRemarks("working");
    }
}
