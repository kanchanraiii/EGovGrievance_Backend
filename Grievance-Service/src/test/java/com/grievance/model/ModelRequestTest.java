package com.grievance.model;

import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRequestTest {

    @Test
    void grievanceModelGetters() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("c1");
        grievance.setDepartmentId("d1");
        grievance.setCategoryCode("cat");
        grievance.setSubCategoryCode("sub");
        grievance.setDescription("desc");
        grievance.setStatus(GrievanceStatus.SUBMITTED);
        LocalDateTime now = LocalDateTime.now();
        grievance.setCreatedAt(now);
        grievance.setUpdatedAt(now);

        assertThat(grievance.getId()).isEqualTo("g1");
        assertThat(grievance.getCitizenId()).isEqualTo("c1");
        assertThat(grievance.getDepartmentId()).isEqualTo("d1");
        assertThat(grievance.getStatus()).isEqualTo(GrievanceStatus.SUBMITTED);
        assertThat(grievance.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void historyModelGetters() {
        GrievanceHistory history = new GrievanceHistory();
        history.setId("h1");
        history.setGrievanceId("g1");
        history.setStatus(GrievanceStatus.ASSIGNED);
        history.setUpdatedBy("u1");
        history.setRemarks("remarks");
        LocalDateTime now = LocalDateTime.now();
        history.setUpdatedAt(now);

        assertThat(history.getId()).isEqualTo("h1");
        assertThat(history.getGrievanceId()).isEqualTo("g1");
        assertThat(history.getStatus()).isEqualTo(GrievanceStatus.ASSIGNED);
        assertThat(history.getRemarks()).isEqualTo("remarks");
        assertThat(history.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void assignmentModelGetters() {
        Assignment assignment = new Assignment();
        assignment.setId("a1");
        assignment.setGrievanceId("g1");
        assignment.setAssignedBy("do");
        assignment.setAssignedTo("cw");
        LocalDateTime now = LocalDateTime.now();
        assignment.setAssignedAt(now);

        assertThat(assignment.getAssignedTo()).isEqualTo("cw");
        assertThat(assignment.getAssignedAt()).isEqualTo(now);
    }

    @Test
    void requestDtosGetters() {
        GrievanceCreateRequest create = new GrievanceCreateRequest();
        create.setCitizenId("c1");
        create.setDepartmentId("d1");
        create.setCategoryCode("cat");
        create.setSubCategoryCode("sub");
        create.setDescription("desc");

        AssignmentRequest assign = new AssignmentRequest();
        assign.setGrievanceId("g1");
        assign.setAssignedBy("do");
        assign.setAssignedTo("cw");

        StatusUpdateRequest statusUpdate = new StatusUpdateRequest();
        statusUpdate.setGrievanceId("g1");
        statusUpdate.setStatus(GrievanceStatus.RESOLVED);
        statusUpdate.setUpdatedBy("u1");
        statusUpdate.setRemarks("ok");

        assertThat(create.getCitizenId()).isEqualTo("c1");
        assertThat(assign.getAssignedTo()).isEqualTo("cw");
        assertThat(statusUpdate.getStatus()).isEqualTo(GrievanceStatus.RESOLVED);
    }
}
