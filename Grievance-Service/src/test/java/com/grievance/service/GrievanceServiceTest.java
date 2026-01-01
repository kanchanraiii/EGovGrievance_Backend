package com.grievance.service;

import com.grievance.model.Assignment;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.GrievanceStatus;
import com.grievance.repository.AssignmentRepository;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.StatusHistoryRepository;
import com.grievance.request.GrievanceCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private GrievanceService grievanceService;

    @Test
    void createGrievance_savesAndLogsHistory() {
        GrievanceCreateRequest request = new GrievanceCreateRequest();
        request.setCitizenId("c1");
        request.setDepartmentId("dept1");
        request.setCategoryCode("cat");
        request.setSubCategoryCode("sub");
        request.setDescription("A valid description");

        when(departmentValidationService.validateDepartment("dept1", "cat", "sub"))
                .thenReturn(Mono.empty());

        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenAnswer(invocation -> {
            GrievanceHistory history = invocation.getArgument(0);
            setId(history, "h1");
            return Mono.just(history);
        });
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> {
            Grievance g = invocation.getArgument(0);
            setId(g, "g1");
            return Mono.just(g);
        });

        StepVerifier.create(grievanceService.createGrievance(request))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo("g1");
                    assertThat(saved.getStatus()).isEqualTo(GrievanceStatus.SUBMITTED);
                    assertThat(saved.getCreatedAt()).isNotNull();
                })
                .verifyComplete();

        verify(statusHistoryRepository).save(any(GrievanceHistory.class));
    }

    @Test
    void assignGrievance_updatesAssignmentAndHistory() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        grievance.setCitizenId("c1");
        grievance.setStatus(GrievanceStatus.SUBMITTED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(Mono.just(new Assignment()));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenAnswer(invocation -> {
            GrievanceHistory history = invocation.getArgument(0);
            setId(history, "h1");
            return Mono.just(history);
        });

        StepVerifier.create(grievanceService.assignGrievance("g1", "do-1", "cw-1"))
                .assertNext(updated -> {
                    assertThat(updated.getAssignedWokerId()).isEqualTo("cw-1");
                    assertThat(updated.getStatus()).isEqualTo(GrievanceStatus.ASSIGNED);
                    assertThat(updated.getAssignedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void updateStatus_changesStatusAndLogsHistory() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        grievance.setStatus(GrievanceStatus.SUBMITTED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(grievanceService.updateStatus("g1", GrievanceStatus.RESOLVED, "user1", "done"))
                .assertNext(updated -> assertThat(updated.getStatus()).isEqualTo(GrievanceStatus.RESOLVED))
                .verifyComplete();

        verify(statusHistoryRepository).save(any(GrievanceHistory.class));
    }

    @Test
    void getStatusHistory_returnsFlux() {
        GrievanceHistory h1 = new GrievanceHistory();
        setId(h1, "h1");
        h1.setGrievanceId("g1");
        h1.setStatus(GrievanceStatus.SUBMITTED);
        h1.setUpdatedBy("c1");

        when(statusHistoryRepository.findByGrievanceIdOrderByUpdatedAtAsc("g1"))
                .thenReturn(Flux.just(h1));

        StepVerifier.create(grievanceService.getStatusHistory("g1"))
                .expectNext(h1)
                .verifyComplete();
    }

    @Test
    void assignGrievance_returnsErrorWhenMissing() {
        when(grievanceRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.assignGrievance("missing", "do", "cw"))
                .expectErrorMessage("Grievance not found")
                .verify();
    }

    @Test
    void updateStatus_returnsErrorWhenMissing() {
        when(grievanceRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.updateStatus("missing", GrievanceStatus.RESOLVED, "u1", "x"))
                .expectErrorMessage("Grievance not found")
                .verify();
    }

    @Test
    void getById_returnsItem() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.getById("g1"))
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void getById_errorsWhenMissing() {
        when(grievanceRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(grievanceService.getById("nope"))
                .expectErrorMessage("Grievance not found")
                .verify();
    }

    @Test
    void getAll_returnsFluxFromRepository() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        when(grievanceRepository.findAll()).thenReturn(Flux.just(grievance));

        StepVerifier.create(grievanceService.getAll())
                .expectNext(grievance)
                .verifyComplete();
    }

    @Test
    void escalateGrievance_updatesAndLogsHistory() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        grievance.setEscalated(false);
        grievance.setStatus(GrievanceStatus.ASSIGNED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));
        when(grievanceRepository.save(any(Grievance.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusHistoryRepository.save(any(GrievanceHistory.class))).thenReturn(Mono.just(new GrievanceHistory()));

        StepVerifier.create(grievanceService.escalateGrievance("g1", "manager"))
                .assertNext(updated -> {
                    assertThat(updated.getStatus()).isEqualTo(GrievanceStatus.ESCALATED);
                    assertThat(updated.isEscalated()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void escalateGrievance_returnsExistingWhenAlreadyEscalated() {
        Grievance grievance = new Grievance();
        setId(grievance, "g1");
        grievance.setEscalated(true);
        grievance.setStatus(GrievanceStatus.ESCALATED);

        when(grievanceRepository.findById("g1")).thenReturn(Mono.just(grievance));

        StepVerifier.create(grievanceService.escalateGrievance("g1", "manager"))
                .expectNext(grievance)
                .verifyComplete();

        verify(statusHistoryRepository, never()).save(any(GrievanceHistory.class));
    }

    private void setId(Object target, String id) {
        try {
            Field f = target.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception ignored) {
        }
    }
}
