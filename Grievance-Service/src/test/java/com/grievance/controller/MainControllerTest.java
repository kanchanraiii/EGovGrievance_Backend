package com.grievance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;

import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.GrievanceStatus;
import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;
import com.grievance.service.GrievanceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class MainControllerTest {

    @Mock
    private GrievanceService grievanceService;

    @InjectMocks
    private MainController controller;

    private Jwt citizenJwt;
    private Jwt officerJwt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        citizenJwt = jwtWith("citizen-1", "CITIZEN", "D1");
        officerJwt = jwtWith("officer-1", "DEPARTMENT_OFFICER", "D1");
    }

    @Test
    void createGrievanceReturnsCreatedId() {
        Grievance saved = new Grievance();
        saved.setId("g1");
        saved.setStatus(GrievanceStatus.SUBMITTED);

        GrievanceCreateRequest request = new GrievanceCreateRequest();
        request.setDepartmentId("D1");
        request.setCategoryCode("CAT");
        request.setSubCategoryCode("SUB");
        request.setDescription("description text");

        when(grievanceService.createGrievance(request, "citizen-1")).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.createGrievance(request, citizenJwt))
                .assertNext(map -> assertThat(map.get("grievanceId")).isEqualTo("g1"))
                .verifyComplete();
    }

    @Test
    void assignGrievanceReturnsStatusMap() {
        AssignmentRequest request = new AssignmentRequest();
        request.setGrievanceId("g1");
        request.setAssignedTo("worker-1");

        Grievance updated = new Grievance();
        updated.setId("g1");
        updated.setStatus(GrievanceStatus.ASSIGNED);
        updated.setAssignedWokerId("worker-1");

        when(grievanceService.assignGrievance("g1", "officer-1", "worker-1", "DEPARTMENT_OFFICER", "D1"))
                .thenReturn(Mono.just(updated));

        StepVerifier.create(controller.assignGrievance(request, officerJwt))
                .assertNext(result -> {
                    assertThat(result.get("status")).isEqualTo("ASSIGNED");
                    assertThat(result.get("assignedTo")).isEqualTo("worker-1");
                })
                .verifyComplete();
    }

    @Test
    void updateStatusDelegatesToService() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setGrievanceId("g1");
        request.setStatus(GrievanceStatus.RESOLVED);
        request.setRemarks("done");

        Grievance updated = new Grievance();
        updated.setId("g1");
        updated.setStatus(GrievanceStatus.RESOLVED);

        when(grievanceService.updateStatus("g1", GrievanceStatus.RESOLVED, "officer-1", "done", "DEPARTMENT_OFFICER", "D1"))
                .thenReturn(Mono.just(updated));

        StepVerifier.create(controller.updateStatus(request, officerJwt))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void getStatusHistoryReturnsFlux() {
        GrievanceHistory history = new GrievanceHistory();
        history.setId("h1");
        when(grievanceService.getStatusHistory("g1", "CITIZEN", "D1")).thenReturn(Flux.just(history));

        StepVerifier.create(controller.getStatusHistory("g1", citizenJwt))
                .expectNext(history)
                .verifyComplete();
    }

    @Test
    void getAllGrievancesUsesRoleAndDepartment() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        when(grievanceService.getAllForRole("CITIZEN", "D1")).thenReturn(Flux.just(grievance));

        StepVerifier.create(controller.getAllGrievances(citizenJwt))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getByDepartmentUsesRequesterContext() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        when(grievanceService.getByDepartment("D1", "DEPARTMENT_OFFICER", "D1")).thenReturn(Flux.just(grievance));

        StepVerifier.create(controller.getByDepartment("D1", officerJwt))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getMyGrievancesUsesSubject() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        when(grievanceService.getByCitizen("citizen-1")).thenReturn(Flux.just(grievance));

        StepVerifier.create(controller.getMyGrievances(citizenJwt))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getMyCaseWorkersUsesOfficer() {
        when(grievanceService.getCaseWorkersForOfficer("officer-1", "DEPARTMENT_OFFICER"))
                .thenReturn(Flux.just("cw-1", "cw-2"));

        StepVerifier.create(controller.getMyCaseWorkers(officerJwt))
                .expectNext("cw-1", "cw-2")
                .verifyComplete();
    }

    private Jwt jwtWith(String subject, String role, String departmentId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("role", role)
                .claim("departmentId", departmentId)
                .build();
    }
}
