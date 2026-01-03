package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ModelRequestTest {

    @Test
    void grievanceModelStoresFields() {
        LocalDateTime now = LocalDateTime.now();
        Grievance grievance = spy(new Grievance());
        grievance.setId("g1");
        grievance.setCitizenId("c1");
        grievance.setDepartmentId("D1");
        grievance.setCategoryCode("CAT");
        grievance.setSubCategoryCode("SUB");
        grievance.setDescription("desc");
        grievance.setStatus(GrievanceStatus.SUBMITTED);
        grievance.setCreatedAt(now);
        grievance.setUpdatedAt(now);

        assertThat(grievance.getId()).isEqualTo("g1");
        assertThat(grievance.getCitizenId()).isEqualTo("c1");
        assertThat(grievance.getDepartmentId()).isEqualTo("D1");
        assertThat(grievance.getStatus()).isEqualTo(GrievanceStatus.SUBMITTED);
        verify(grievance).setDescription("desc");
    }

    @Test
    void assignmentAndHistoryHoldValues() {
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment = spy(new Assignment());
        assignment.setId(UUID.randomUUID().toString());
        assignment.setGrievanceId("g1");
        assignment.setAssignedBy("officer");
        assignment.setAssignedTo("worker");
        assignment.setAssignedAt(now);

        GrievanceHistory history = spy(new GrievanceHistory());
        history.setId("h1");
        history.setGrievanceId("g1");
        history.setStatus(GrievanceStatus.ASSIGNED);
        history.setUpdatedBy("officer");
        history.setRemarks("assigned");
        history.setUpdatedAt(now);

        assertThat(assignment.getAssignedAt()).isEqualTo(now);
        assertThat(history.getStatus()).isEqualTo(GrievanceStatus.ASSIGNED);
        assertThat(history.getRemarks()).isEqualTo("assigned");
        verify(history).setUpdatedBy("officer");
        verify(assignment).setAssignedTo("worker");
    }

    @Test
    void requestDtosExposeGetters() {
        GrievanceCreateRequest create = spy(new GrievanceCreateRequest());
        create.setDepartmentId("D1");
        create.setCategoryCode("CAT");
        create.setSubCategoryCode("SUB");
        create.setDescription("description text");

        AssignmentRequest assign = spy(new AssignmentRequest());
        assign.setGrievanceId("g1");
        assign.setAssignedTo("worker-1");

        StatusUpdateRequest update = spy(new StatusUpdateRequest());
        update.setGrievanceId("g1");
        update.setStatus(GrievanceStatus.RESOLVED);
        update.setRemarks("done");

        assertThat(create.getDepartmentId()).isEqualTo("D1");
        assertThat(assign.getAssignedTo()).isEqualTo("worker-1");
        assertThat(update.getStatus()).isEqualTo(GrievanceStatus.RESOLVED);
        verify(update).setRemarks("done");
        verify(assign).setAssignedTo("worker-1");
    }
}
