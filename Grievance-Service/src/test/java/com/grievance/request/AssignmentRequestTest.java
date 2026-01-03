package com.grievance.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignmentRequestTest {

    @Test
    void settersAndGettersWork() {
        AssignmentRequest request = spy(new AssignmentRequest());
        request.setGrievanceId("g1");
        request.setAssignedTo("worker");

        assertThat(request.getGrievanceId()).isEqualTo("g1");
        assertThat(request.getAssignedTo()).isEqualTo("worker");

        verify(request).setGrievanceId("g1");
        verify(request).setAssignedTo("worker");
    }
}
