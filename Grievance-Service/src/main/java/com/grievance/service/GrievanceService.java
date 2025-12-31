package com.grievance.service;

import com.grievance.model.*;
import com.grievance.repository.AssignmentRepository;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.StatusHistoryRepository;
import com.grievance.request.GrievanceCreateRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class GrievanceService {

	@Autowired
	private GrievanceRepository grievanceRepository;

	@Autowired
	private AssignmentRepository assignmentRepository;

	@Autowired
	private StatusHistoryRepository statusHistoryRepository;
	
	@Autowired
	private DepartmentValidationService departmentValidationService;

	// to create a grievance
	public Mono<Grievance> createGrievance(GrievanceCreateRequest request) {

	    return departmentValidationService
	            .validateDepartment(
	                    request.getDepartmentId(),    
	                    request.getCategoryCode(),
	                    request.getSubCategoryCode()
	            )
	            .then(Mono.defer(() -> {

	                Grievance grievance = new Grievance();
	                grievance.setCitizenId(request.getCitizenId());
	                grievance.setDepartmentId(request.getDepartmentId());
	                grievance.setCategoryCode(request.getCategoryCode());
	                grievance.setSubCategoryCode(request.getSubCategoryCode());
	                grievance.setDescription(request.getDescription());
	                grievance.setStatus(GrievanceStatus.SUBMITTED);
	                grievance.setEscalated(false);
	                grievance.setCreatedAt(LocalDateTime.now());
	                grievance.setUpdatedAt(LocalDateTime.now());

	                return grievanceRepository.save(grievance)
	                        .flatMap(saved ->
	                                saveStatusHistory(
	                                        saved.getId(),
	                                        GrievanceStatus.SUBMITTED,
	                                        saved.getCitizenId(),
	                                        "Grievance submitted"
	                                ).thenReturn(saved)
	                        );
	            }));
	}


	// to assign a grievance - assigned by Dept Officer to a Case Worker
	public Mono<Grievance> assignGrievance(String grievanceId, String assignedBy, String assignedTo) {

		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new RuntimeException("Grievance not found"))).flatMap(grievance -> {

					// 1️. update grievance state
					grievance.setAssignedWokerId(assignedTo);
					grievance.setStatus(GrievanceStatus.ASSIGNED);
					grievance.setAssignedAt(LocalDateTime.now());
					grievance.setUpdatedAt(LocalDateTime.now());

					// 2️. create assignment record
					Assignment assignment = new Assignment();
					assignment.setGrievanceId(grievanceId);
					assignment.setAssignedBy(assignedBy);
					assignment.setAssignedTo(assignedTo);
					assignment.setAssignedAt(LocalDateTime.now());

					// 3. save assignment -> grievance and status history
					return assignmentRepository.save(assignment).then(grievanceRepository.save(grievance))
							.flatMap(updatedGrievance -> saveStatusHistory(grievanceId, GrievanceStatus.ASSIGNED,
									assignedBy, "Assigned to case worker").thenReturn(updatedGrievance));
				});
	}

	// to update the status of a grievance - done by dept officer / case worker
	public Mono<Grievance> updateStatus(String grievanceId, GrievanceStatus status, String updatedBy, String remarks) {

		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new RuntimeException("Grievance not found"))).flatMap(grievance -> {

					grievance.setStatus(status);
					grievance.setUpdatedAt(LocalDateTime.now());

					return grievanceRepository.save(grievance).flatMap(
							updated -> saveStatusHistory(grievanceId, status, updatedBy, remarks).thenReturn(updated));
				});
	}

	// mono to get one object of grievance - get by id
	public Mono<Grievance> getById(String grievanceId) {
		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new RuntimeException("Grievance not found")));
	}

	// flux to get multiple objects - get all
	public Flux<Grievance> getAll() {
		return grievanceRepository.findAll();
	}

	// flux to get multiple objects - get status history
	public Flux<GrievanceHistory> getStatusHistory(String grievanceId) {
		return statusHistoryRepository.findByGrievanceIdOrderByUpdatedAtAsc(grievanceId)
				.switchIfEmpty(Mono.error(new RuntimeException("Grievance not found")));
	}

	// helper function
	private Mono<GrievanceHistory> saveStatusHistory(String grievanceId, GrievanceStatus status, String updatedBy,
			String remarks) {

		GrievanceHistory history = new GrievanceHistory();
		history.setGrievanceId(grievanceId);
		history.setStatus(status);
		history.setUpdatedBy(updatedBy);
		history.setRemarks(remarks);
		history.setUpdatedAt(LocalDateTime.now());

		return statusHistoryRepository.save(history);
	}
	
	// function to check if SLA is breached
	private boolean isSLABreached(Grievance grievance, int slaDays) {
		return grievance.getAssignedAt() != null &&
		           grievance.getAssignedAt().isBefore(LocalDateTime.now().minusDays(slaDays));
	}
	
	// escalation method
	public Mono<Grievance> escalateGrievance(String grievanceId, String escalatedBy) {

	    return grievanceRepository.findById(grievanceId)
	        .flatMap(grievance -> {

	            if (grievance.isEscalated()) {
	                return Mono.just(grievance);
	            }

	            grievance.setStatus(GrievanceStatus.ESCALATED);
	            grievance.setEscalated(true);
	            grievance.setUpdatedAt(LocalDateTime.now());

	        return grievanceRepository.save(grievance)
	                .flatMap(updated ->
	                    saveStatusHistory(
	                        grievanceId,
	                        GrievanceStatus.ESCALATED,
	                        escalatedBy,
	                        "SLA breached escalated to supervisory officer"
	                    ).thenReturn(updated)
	                );
	        });
	}


}
