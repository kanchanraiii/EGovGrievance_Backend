package com.grievance.controller;

import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.GrievanceStatus;
import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;
import com.grievance.service.GrievanceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(MainController.class)
class MainControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GrievanceService grievanceService;

    @Test
    void createGrievance_returnsCreated() {
        GrievanceCreateRequest request = new GrievanceCreateRequest();
        request.setCitizenId("c1");
        request.setDepartmentId("d1");
        request.setCategoryCode("cat");
        request.setSubCategoryCode("sub");
        request.setDescription("Description for grievance");

        Grievance saved = new Grievance();
        saved.setId("g1");
        saved.setCitizenId("c1");
        saved.setStatus(GrievanceStatus.SUBMITTED);
        saved.setCreatedAt(LocalDateTime.now());

        when(grievanceService.createGrievance(any())).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/api/grievances/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.status").isEqualTo("SUBMITTED");
    }

    @Test
    void getGrievanceById_returnsOk() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("c1");

        when(grievanceService.getById("g1")).thenReturn(Mono.just(grievance));

        webTestClient.get()
                .uri("/api/grievances/g1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.citizenId").isEqualTo("c1");
    }

    @Test
    void getAllGrievances_returnsList() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");

        when(grievanceService.getAll()).thenReturn(Flux.just(grievance));

        webTestClient.get()
                .uri("/api/grievances/getAll")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("g1");
    }

    @Test
    void assignGrievance_returnsUpdated() {
        AssignmentRequest request = new AssignmentRequest();
        request.setGrievanceId("g1");
        request.setAssignedBy("do");
        request.setAssignedTo("cw");

        Grievance updated = new Grievance();
        updated.setId("g1");
        updated.setAssignedWokerId("cw");
        updated.setStatus(GrievanceStatus.ASSIGNED);

        when(grievanceService.assignGrievance("g1", "do", "cw")).thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/grievances/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ASSIGNED")
                .jsonPath("$.assignedWokerId").isEqualTo("cw");
    }

    @Test
    void updateStatus_returnsUpdated() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setGrievanceId("g1");
        request.setStatus(GrievanceStatus.RESOLVED);
        request.setUpdatedBy("cw");
        request.setRemarks("done");

        Grievance updated = new Grievance();
        updated.setId("g1");
        updated.setStatus(GrievanceStatus.RESOLVED);

        when(grievanceService.updateStatus("g1", GrievanceStatus.RESOLVED, "cw", "done"))
                .thenReturn(Mono.just(updated));

        webTestClient.patch()
                .uri("/api/grievances/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("RESOLVED");
    }

    @Test
    void getStatusHistory_returnsFlux() {
        GrievanceHistory history = new GrievanceHistory();
        history.setId("h1");
        history.setGrievanceId("g1");
        history.setStatus(GrievanceStatus.SUBMITTED);

        when(grievanceService.getStatusHistory("g1")).thenReturn(Flux.just(history));

        webTestClient.get()
                .uri("/api/grievances/history/g1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("h1");
    }
}
