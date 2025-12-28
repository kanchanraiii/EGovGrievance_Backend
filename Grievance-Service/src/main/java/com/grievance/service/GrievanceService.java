package com.grievance.service;

import com.grievance.model.*;
import com.grievance.repository.AssignmentRepository;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public GrievanceService(GrievanceRepository grievanceRepository,
                            AssignmentRepository assignmentRepository,
                            StatusHistoryRepository statusHistoryRepository) {
        this.grievanceRepository = grievanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

   

    
    // to create a grievance
    public Mono<Grievance> createGrievance(Grievance grievance) {
        grievance.setStatus(GrievanceStatus.SUBMITTED);
        grievance.setCreatedAt(LocalDateTime.now());
        grievance.setUpdatedAt(LocalDateTime.now());

        return grievanceRepository.save(grievance)
                .flatMap(saved -> saveStatusHistory(
                        saved.getId(),
                        GrievanceStatus.SUBMITTED,
                        saved.getCitizenId(),
                        "Grievance submitted"
                ).thenReturn(saved));
    }

    
}
