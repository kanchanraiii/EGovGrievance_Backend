package com.grievance.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ModelGettersTest {

    @Test
    void grievanceGettersReturnAssignedValues() {
        LocalDateTime now = LocalDateTime.now();
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("c1");
        grievance.setDepartmentId("D1");
        grievance.setAssignedWokerId("cw-1");
        grievance.setCategoryCode("CAT");
        grievance.setSubCategoryCode("SUB");
        grievance.setDescription("A long enough description");
        grievance.setStatus(GrievanceStatus.IN_PROGRESS);
        grievance.setCreatedAt(now);
        grievance.setUpdatedAt(now.plusDays(1));
        grievance.setAssignedAt(now.minusHours(1));
        grievance.setEscalated(true);

        assertThat(grievance.getId()).isEqualTo("g1");
        assertThat(grievance.getCitizenId()).isEqualTo("c1");
        assertThat(grievance.getDepartmentId()).isEqualTo("D1");
        assertThat(grievance.getAssignedWokerId()).isEqualTo("cw-1");
        assertThat(grievance.getCategoryCode()).isEqualTo("CAT");
        assertThat(grievance.getSubCategoryCode()).isEqualTo("SUB");
        assertThat(grievance.getDescription()).contains("long enough");
        assertThat(grievance.getStatus()).isEqualTo(GrievanceStatus.IN_PROGRESS);
        assertThat(grievance.getCreatedAt()).isEqualTo(now);
        assertThat(grievance.getUpdatedAt()).isEqualTo(now.plusDays(1));
        assertThat(grievance.getAssignedAt()).isEqualTo(now.minusHours(1));
        assertThat(grievance.isEscalated()).isTrue();
    }

    @Test
    void grievanceHistoryGettersReturnAssignedValues() {
        LocalDateTime time = LocalDateTime.now();
        GrievanceHistory history = new GrievanceHistory();
        history.setId("h1");
        history.setGrievanceId("g1");
        history.setStatus(GrievanceStatus.CLOSED);
        history.setUpdatedBy("user-1");
        history.setRemarks("done");
        history.setUpdatedAt(time);

        assertThat(history.getId()).isEqualTo("h1");
        assertThat(history.getGrievanceId()).isEqualTo("g1");
        assertThat(history.getStatus()).isEqualTo(GrievanceStatus.CLOSED);
        assertThat(history.getUpdatedBy()).isEqualTo("user-1");
        assertThat(history.getRemarks()).isEqualTo("done");
        assertThat(history.getUpdatedAt()).isEqualTo(time);
    }

    @Test
    void assignmentGettersReturnAssignedValues() {
        LocalDateTime assigned = LocalDateTime.now();
        Assignment assignment = new Assignment();
        assignment.setId("a1");
        assignment.setGrievanceId("g1");
        assignment.setAssignedBy("officer-1");
        assignment.setAssignedTo("worker-1");
        assignment.setAssignedAt(assigned);

        assertThat(assignment.getId()).isEqualTo("a1");
        assertThat(assignment.getGrievanceId()).isEqualTo("g1");
        assertThat(assignment.getAssignedBy()).isEqualTo("officer-1");
        assertThat(assignment.getAssignedTo()).isEqualTo("worker-1");
        assertThat(assignment.getAssignedAt()).isEqualTo(assigned);
    }

    @Test
    void escalatedGrievanceViewGettersReturnValues() {
        EscalatedGrievanceView view = new EscalatedGrievanceView(
                "g1",
                "desc",
                "D1",
                "cw-1",
                "officer-1",
                GrievanceStatus.ESCALATED
        );

        assertThat(view.getGrievanceId()).isEqualTo("g1");
        assertThat(view.getDescription()).isEqualTo("desc");
        assertThat(view.getDepartmentId()).isEqualTo("D1");
        assertThat(view.getAssignedTo()).isEqualTo("cw-1");
        assertThat(view.getAssignedBy()).isEqualTo("officer-1");
        assertThat(view.getStatus()).isEqualTo(GrievanceStatus.ESCALATED);
    }

    @Test
    void fileMetadataAndEscalatedViewSetterPathsCovered() {
        FileMetadata metadata = new FileMetadata();
        metadata.setId("f1");
        metadata.setFileName("doc.pdf");
        metadata.setContentType("application/pdf");
        metadata.setGrievanceId("g1");
        metadata.setUploadedBy("user-1");

        assertThat(metadata.getId()).isEqualTo("f1");
        assertThat(metadata.getFileName()).isEqualTo("doc.pdf");
        assertThat(metadata.getContentType()).isEqualTo("application/pdf");
        assertThat(metadata.getGrievanceId()).isEqualTo("g1");
        assertThat(metadata.getUploadedBy()).isEqualTo("user-1");

        EscalatedGrievanceView view = new EscalatedGrievanceView();
        view.setGrievanceId("g2");
        view.setDescription("desc2");
        view.setDepartmentId("D2");
        view.setAssignedTo("cw-2");
        view.setAssignedBy("officer-2");
        view.setStatus(GrievanceStatus.RESOLVED);

        assertThat(view.getGrievanceId()).isEqualTo("g2");
        assertThat(view.getDescription()).isEqualTo("desc2");
        assertThat(view.getDepartmentId()).isEqualTo("D2");
        assertThat(view.getAssignedTo()).isEqualTo("cw-2");
        assertThat(view.getAssignedBy()).isEqualTo("officer-2");
        assertThat(view.getStatus()).isEqualTo(GrievanceStatus.RESOLVED);
    }
}
