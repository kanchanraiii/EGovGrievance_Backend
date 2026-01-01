package com.grievance.scheduler;

import com.grievance.model.Grievance;
import com.grievance.model.GrievanceStatus;
import com.grievance.repository.GrievanceRepository;
import com.grievance.service.GrievanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrievanceSchedulerTest {

    @Mock
    private GrievanceRepository grievanceRepository;

    @Mock
    private GrievanceService grievanceService;

    @InjectMocks
    private GrievanceScheduler scheduler;

    @Test
    void checkSlaBreaches_escalatesOverdueGrievances() {
        Grievance overdue = new Grievance();
        overdue.setId("g1");
        overdue.setStatus(GrievanceStatus.ASSIGNED);
        overdue.setAssignedAt(LocalDateTime.now().minusDays(8));
        overdue.setEscalated(false);

        when(grievanceRepository.findByStatusInAndAssignedAtBeforeAndEscalatedFalse(
                eq(List.of(GrievanceStatus.ASSIGNED, GrievanceStatus.IN_PROGRESS)),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(Flux.just(overdue));

        when(grievanceService.escalateGrievance("g1", "SYSTEM")).thenReturn(Mono.just(overdue));

        scheduler.checkSlaBreaches();

        verify(grievanceService, timeout(500)).escalateGrievance("g1", "SYSTEM");
    }
}
