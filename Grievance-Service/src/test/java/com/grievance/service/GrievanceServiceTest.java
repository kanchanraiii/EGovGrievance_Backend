package com.grievance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.grievance.model.Assignment;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.GrievanceStatus;
import com.grievance.repository.AssignmentRepository;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.StatusHistoryRepository;
import com.grievance.request.GrievanceCreateRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GrievanceServiceTest {

    @Mock
    private GrievanceRepository grievanceRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private StatusHistoryRepository statusHistoryRepository;
    @Mock
    private DepartmentValidationService departmentValidationService;
    @Mock
    private GrievanceEventPublisher grievanceEventPublisher;

    @InjectMocks
    private GrievanceService grievanceService;

    private Grievance grievanceWithDept(String departmentId) {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId(departmentId);
        return grievance;
    }

    @Test
    void createGrievancePersistsAndPublishesEvent() {
        GrievanceCreateRequest request = new GrievanceCreateRequest();
        request.setDepartmentId("D1");
        request.setCategoryCode("CAT");
        request.setSubCategoryCode("SUB");
        request.setDescription("Description for grievance");

        when(departmentValidationService.validateDepartment("D1", "CAT", "SUB")).thenReturn(Mono.empty());

        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> {
            Grievance saved = invocation.getArgument(0);
            saved.setId("g1");
            return Mono.just(saved);
        });
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.createGrievance(request, "citizen-1"))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo("g1");
                    assertThat(saved.getCitizenId()).isEqualTo("citizen-1");
                    assertThat(saved.getStatus()).isEqualTo(GrievanceStatus.SUBMITTED);
                })
                .verifyComplete();

        verify(grievanceEventPublisher).publishStatusChange(any(), any(), any());
    }

    @Test
    void assignGrievanceSetsAssignmentWhenAllowed() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId("D1");
        grievance.setStatus(GrievanceStatus.SUBMITTED);

        when(grievanceRepository.findByAssignedWokerId("worker1")).thenReturn(Flux.empty());
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(Mono.just(new Assignment()));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.assignGrievance("g1", "officer", "worker1", "DEPARTMENT_OFFICER", "D1"))
                .assertNext(updated -> {
                    assertThat(updated.getAssignedWokerId()).isEqualTo("worker1");
                    assertThat(updated.getStatus()).isEqualTo(GrievanceStatus.ASSIGNED);
                })
                .verifyComplete();
    }

    @Test
    void assignGrievanceRejectsWhenAlreadyAssigned() {
        Grievance grievance = grievanceWithDept("D1");
        grievance.setAssignedWokerId("existing");
        grievance.setStatus(GrievanceStatus.ASSIGNED);

        when(grievanceRepository.findByAssignedWokerId("worker1")).thenReturn(Flux.empty());
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.assignGrievance("g1", "officer", "worker1", "DEPARTMENT_OFFICER", "D1"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void assignGrievanceRejectsWhenCaseWorkerBusy() {
        Grievance activeAssignment = grievanceWithDept("D1");
        activeAssignment.setStatus(GrievanceStatus.IN_PROGRESS);

        when(grievanceRepository.findByAssignedWokerId("worker1")).thenReturn(Flux.just(activeAssignment));
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievanceWithDept("D1")));

        StepVerifier.create(grievanceService.assignGrievance("g1", "officer", "worker1", "DEPARTMENT_OFFICER", "D1"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void assignGrievanceIgnoresCompletedAssignments() {
        Grievance resolved = grievanceWithDept("D1");
        resolved.setStatus(GrievanceStatus.RESOLVED);
        Grievance workDone = grievanceWithDept("D1");
        workDone.setStatus(GrievanceStatus.WORK_DONE);
        Grievance closed = grievanceWithDept("D1");
        closed.setStatus(GrievanceStatus.CLOSED);

        Grievance grievance = grievanceWithDept("D1");
        grievance.setId("g1");

        when(grievanceRepository.findByAssignedWokerId("worker1")).thenReturn(Flux.just(resolved, workDone, closed));
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(Mono.just(new Assignment()));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.assignGrievance("g1", "officer", "worker1", "DEPARTMENT_OFFICER", "D1"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void assignGrievanceRejectsWhenDepartmentMismatch() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId("D1");

        when(grievanceRepository.findByAssignedWokerId("worker1")).thenReturn(Flux.empty());
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.assignGrievance("g1", "officer", "worker1", "DEPARTMENT_OFFICER", "OTHER"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                })
                .verify();

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void updateStatusValidatesDepartmentForRestrictedRole() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId("D1");
        grievance.setStatus(GrievanceStatus.SUBMITTED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.RESOLVED, "user1", "done", "CASE_WORKER", "D1"))
                .assertNext(updated -> assertThat(updated.getStatus()).isEqualTo(GrievanceStatus.RESOLVED))
                .verifyComplete();
    }

    @Test
    void updateStatusRejectsWrongDepartment() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId("D1");

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.IN_PROGRESS, "user1", "review", "CASE_WORKER", "OTHER"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void updateStatusAllowsNullRoleAcrossDepartments() {
        Grievance grievance = grievanceWithDept("D1");
        grievance.setId("g1");

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.RESOLVED, "user1", "done", null, "OTHER"))
                .expectNextMatches(updated -> updated.getStatus() == GrievanceStatus.RESOLVED)
                .verifyComplete();
    }

    @Test
    void updateStatusRejectsRestrictedRoleWithoutDepartment() {
        Grievance grievance = grievanceWithDept("D1");
        grievance.setId("g1");

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.IN_PROGRESS, "user1", "review", "DEPARTMENT_OFFICER", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void updateStatusRejectsWhenGrievanceDepartmentMissingForRestrictedRole() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId(null);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.IN_PROGRESS, "user1", "review", "DEPARTMENT_OFFICER", "D1"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getAllForRoleUsesDepartmentFilterForRestrictedRoles() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");

        when(grievanceRepository.findByDepartmentId("D1")).thenReturn(Flux.just(grievance));
        when(grievanceRepository.findAll()).thenReturn(Flux.just(grievance));

        StepVerifier.create(grievanceService.getAllForRole("DEPARTMENT_OFFICER", "D1"))
                .expectNext(grievance)
                .verifyComplete();

        StepVerifier.create(grievanceService.getAllForRole("ADMIN", null))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getAllForRoleRequiresDepartmentForRestrictedRole() {
        StepVerifier.create(grievanceService.getAllForRole("DEPARTMENT_OFFICER", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getStatusHistoryValidatesDepartment() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setDepartmentId("D1");

        GrievanceHistory history = new GrievanceHistory();
        history.setId("h1");
        history.setGrievanceId("g1");

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(statusHistoryRepository.findByGrievanceIdOrderByUpdatedAtAsc("g1")).thenReturn(Flux.just(history));

        StepVerifier.create(grievanceService.getStatusHistory("g1", "CASE_WORKER", "D1"))
                .expectNext(history)
                .verifyComplete();
    }

    @Test
    void getStatusHistoryRejectsWhenDifferentDepartment() {
        Grievance grievance = grievanceWithDept("D1");
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.getStatusHistory("g1", "CASE_WORKER", "D2"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getStatusHistoryRejectsWhenGrievanceMissing() {
        when(grievanceRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.getStatusHistory("missing", "CITIZEN", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getStatusHistoryRejectsWhenHistoryMissing() {
        Grievance grievance = grievanceWithDept("D1");
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(statusHistoryRepository.findByGrievanceIdOrderByUpdatedAtAsc("g1")).thenReturn(Flux.empty());

        StepVerifier.create(grievanceService.getStatusHistory("g1", "CITIZEN", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByDepartmentRejectsCrossDepartmentAccess() {
        StepVerifier.create(grievanceService.getByDepartment("D1", "DEPARTMENT_OFFICER", "D2"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByDepartmentAllowsSupervisoryOfficer() {
        Grievance grievance = grievanceWithDept("D1");
        when(grievanceRepository.findByDepartmentId("D1")).thenReturn(Flux.just(grievance));

        StepVerifier.create(grievanceService.getByDepartment("D1", "SUPERVISORY_OFFICER", "IGNORED"))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getByDepartmentAllowsNullRole() {
        StepVerifier.create(grievanceService.getByDepartment("D1", null, null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByDepartmentRejectsWhenRestrictedRoleMissingRequesterDepartment() {
        StepVerifier.create(grievanceService.getByDepartment("D1", "DEPARTMENT_OFFICER", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByCitizenReturnsGrievancesForSubject() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        when(grievanceRepository.findByCitizenId("citizen-1")).thenReturn(Flux.just(grievance));

        StepVerifier.create(grievanceService.getByCitizen("citizen-1"))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getByCitizenRejectsNull() {
        StepVerifier.create(grievanceService.getByCitizen(null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByIdRejectsWhenMissing() {
        when(grievanceRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.getById("missing"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getCaseWorkersForOfficerReturnsDistinctIds() {
        Assignment a1 = new Assignment();
        a1.setAssignedTo("cw-1");
        Assignment a2 = new Assignment();
        a2.setAssignedTo("cw-1");
        Assignment a3 = new Assignment();
        a3.setAssignedTo("cw-2");

        when(assignmentRepository.findByAssignedBy("officer-1")).thenReturn(Flux.just(a1, a2, a3));

        StepVerifier.create(grievanceService.getCaseWorkersForOfficer("officer-1", "DEPARTMENT_OFFICER"))
                .expectNext("cw-1", "cw-2")
                .verifyComplete();
    }

    @Test
    void getCaseWorkersForOfficerFiltersOutNullIds() {
        Assignment valid = new Assignment();
        valid.setAssignedTo("cw-1");
        Assignment nullAssignment = new Assignment();

        when(assignmentRepository.findByAssignedBy("officer-1")).thenReturn(Flux.just(valid, nullAssignment));

        StepVerifier.create(grievanceService.getCaseWorkersForOfficer("officer-1", "SUPERVISORY_OFFICER"))
                .expectNext("cw-1")
                .verifyComplete();
    }

    @Test
    void getCaseWorkersForOfficerAllowsAdminRole() {
        Assignment a1 = new Assignment();
        a1.setAssignedTo("cw-1");
        Assignment a2 = new Assignment();
        a2.setAssignedTo("cw-2");

        when(assignmentRepository.findByAssignedBy("officer-1")).thenReturn(Flux.just(a1, a2));

        StepVerifier.create(grievanceService.getCaseWorkersForOfficer("officer-1", "ADMIN"))
                .expectNext("cw-1", "cw-2")
                .verifyComplete();
    }

    @Test
    void getCaseWorkersForOfficerRejectsUnauthorizedRole() {
        StepVerifier.create(grievanceService.getCaseWorkersForOfficer("user-1", "CITIZEN"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByCaseWorkerRequiresIdentifier() {
        StepVerifier.create(grievanceService.getByCaseWorker(null, "DEPARTMENT_OFFICER", "D1"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();
    }

    @Test
    void getByCaseWorkerFiltersByDepartmentForRestrictedRoles() {
        Grievance deptMatch = grievanceWithDept("D1");
        deptMatch.setAssignedWokerId("cw-1");
        Grievance deptOther = grievanceWithDept("D2");
        deptOther.setAssignedWokerId("cw-1");

        when(grievanceRepository.findByAssignedWokerId("cw-1")).thenReturn(Flux.just(deptMatch, deptOther));

        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", "DEPARTMENT_OFFICER", "D1"))
                .expectNext(deptMatch)
                .verifyComplete();
    }

    @Test
    void getByCaseWorkerReturnsAllForAdmin() {
        Grievance deptMatch = grievanceWithDept("D1");
        deptMatch.setAssignedWokerId("cw-1");
        Grievance deptOther = grievanceWithDept("D2");
        deptOther.setAssignedWokerId("cw-1");

        when(grievanceRepository.findByAssignedWokerId("cw-1")).thenReturn(Flux.just(deptMatch, deptOther));

        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", "ADMIN", null))
                .expectNext(deptMatch, deptOther)
                .verifyComplete();
    }

    @Test
    void getByCaseWorkerReturnsAllForSupervisoryOfficer() {
        Grievance deptMatch = grievanceWithDept("D1");
        deptMatch.setAssignedWokerId("cw-1");
        Grievance deptOther = grievanceWithDept("D2");
        deptOther.setAssignedWokerId("cw-1");

        when(grievanceRepository.findByAssignedWokerId("cw-1")).thenReturn(Flux.just(deptMatch, deptOther));

        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", "SUPERVISORY_OFFICER", null))
                .expectNext(deptMatch, deptOther)
                .verifyComplete();
    }

    @Test
    void getByCaseWorkerRejectsUnauthorizedRole() {
        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", "CITIZEN", "D1"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByCaseWorkerRejectsNullRole() {
        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", null, "D1"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getByCaseWorkerRequiresDepartmentForRestrictedRole() {
        StepVerifier.create(grievanceService.getByCaseWorker("cw-1", "DEPARTMENT_OFFICER", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getCaseWorkersForOfficerRejectsNullOfficer() {
        StepVerifier.create(grievanceService.getCaseWorkersForOfficer(null, "ADMIN"))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getCaseWorkersForOfficerRejectsNullRole() {
        StepVerifier.create(grievanceService.getCaseWorkersForOfficer("officer-1", null))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void escalateGrievancePublishesWhenNotEscalated() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("c1");
        grievance.setEscalated(false);
        grievance.setStatus(GrievanceStatus.IN_PROGRESS);
        grievance.setAssignedAt(LocalDateTime.now().minusDays(8));

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));
        when(grievanceEventPublisher.publishStatusChange(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.escalateGrievance("g1", "SYSTEM"))
                .assertNext(updated -> assertThat(updated.isEscalated()).isTrue())
                .verifyComplete();

        verify(grievanceEventPublisher).publishStatusChange(any(), any(), any());
    }

    @Test
    void escalateGrievanceSkipsWhenAlreadyEscalated() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setEscalated(true);
        grievance.setStatus(GrievanceStatus.ESCALATED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.escalateGrievance("g1", "SYSTEM"))
                .expectNext(grievance)
                .verifyComplete();

        verify(statusHistoryRepository, never()).save(any());
        verify(grievanceEventPublisher, never()).publishStatusChange(any(), any(), any());
    }
}
