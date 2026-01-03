package com.grievance.service;

import com.grievance.model.*;
import com.grievance.repository.AssignmentRepository;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.StatusHistoryRepository;
import com.grievance.request.GrievanceCreateRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

	@Autowired
	private GrievanceEventPublisher grievanceEventPublisher;

	// to create a grievance
	public Mono<Grievance> createGrievance(GrievanceCreateRequest request, String citizenId) {

	    return departmentValidationService
	            .validateDepartment(
	                    request.getDepartmentId(),    
	                    request.getCategoryCode(),
	                    request.getSubCategoryCode()
	            )
	            .then(Mono.defer(() -> {

	                Grievance grievance = new Grievance();
	                grievance.setCitizenId(citizenId);
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
	                                ).thenReturn(saved))
	                        .flatMap(saved ->
	                                grievanceEventPublisher
	                                        .publishStatusChange(saved, GrievanceStatus.SUBMITTED, "Grievance submitted")
	                                        .thenReturn(saved));
	            }));
	}


	// to assign a grievance - assigned by Dept Officer to a Case Worker
	public Mono<Grievance> assignGrievance(String grievanceId, String assignedBy, String assignedTo, String requesterRole, String requesterDepartmentId) {

		return ensureCaseWorkerAvailable(assignedTo)
				.then(grievanceRepository.findById(grievanceId))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found")))
				.flatMap(grievance -> ensureSameDepartmentForRestrictedRole(requesterRole, requesterDepartmentId, grievance))
				.flatMap(grievance -> {

					if (grievance.getAssignedWokerId() != null) {
						return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
								"Grievance already assigned to " + grievance.getAssignedWokerId()));
					}

					grievance.setAssignedWokerId(assignedTo);
					grievance.setStatus(GrievanceStatus.ASSIGNED);
					grievance.setAssignedAt(LocalDateTime.now());
					grievance.setUpdatedAt(LocalDateTime.now());

					Assignment assignment = new Assignment();
					assignment.setGrievanceId(grievanceId);
					assignment.setAssignedBy(assignedBy);
					assignment.setAssignedTo(assignedTo);
					assignment.setAssignedAt(LocalDateTime.now());

					return assignmentRepository.save(assignment)
							.then(grievanceRepository.save(grievance))
							.flatMap(updatedGrievance -> saveStatusHistory(grievanceId, GrievanceStatus.ASSIGNED,
											assignedBy, "Assigned to case worker").thenReturn(updatedGrievance))
							.flatMap(updatedGrievance -> grievanceEventPublisher
											.publishStatusChange(updatedGrievance, GrievanceStatus.ASSIGNED,
													"Assigned to case worker " + assignedTo)
											.thenReturn(updatedGrievance));
				});
	}

	// to update the status of a grievance - done by dept officer / case worker
	public Mono<Grievance> updateStatus(String grievanceId, GrievanceStatus status, String updatedBy, String remarks, String requesterRole, String requesterDepartmentId) {

		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found")))
				.flatMap(grievance -> ensureSameDepartmentForRestrictedRole(requesterRole, requesterDepartmentId, grievance))
				.flatMap(grievance -> {

					grievance.setStatus(status);
					grievance.setUpdatedAt(LocalDateTime.now());

					return grievanceRepository.save(grievance).flatMap(
							updated -> saveStatusHistory(grievanceId, status, updatedBy, remarks).thenReturn(updated))
							.flatMap(updated -> grievanceEventPublisher
									.publishStatusChange(updated, status, remarks)
									.thenReturn(updated));
				});
	}

	// mono to get one object of grievance - get by id
	public Mono<Grievance> getById(String grievanceId) {
		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new RuntimeException("Grievance not found")));
	}

	// flux to get multiple objects - get all
	public Flux<Grievance> getAllForRole(String role, String requesterDepartmentId) {
		if (isDepartmentRestrictedRole(role)) {
			if (requesterDepartmentId == null) {
				return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized for this department"));
			}
			return grievanceRepository.findByDepartmentId(requesterDepartmentId);
		}
		return grievanceRepository.findAll();
	}

	// flux to get multiple objects - get status history
	public Flux<GrievanceHistory> getStatusHistory(String grievanceId, String role, String requesterDepartmentId) {
		return grievanceRepository.findById(grievanceId)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found")))
				.flatMap(grievance -> ensureSameDepartmentForRestrictedRole(role, requesterDepartmentId, grievance))
				.flatMapMany(grievance -> statusHistoryRepository.findByGrievanceIdOrderByUpdatedAtAsc(grievanceId)
						.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"))));
	}

	// list grievances by department with access control
	public Flux<Grievance> getByDepartment(String departmentId, String role, String requesterDepartmentId) {
		if (!isSupervisoryOfficer(role)) {
			return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only supervisory officers can view department grievances"));
		}
		return grievanceRepository.findByDepartmentId(departmentId);
	}

	// list grievances assigned to a case worker (department officer / supervisory officer / admin)
	public Flux<Grievance> getByCaseWorker(String caseWorkerId, String role, String requesterDepartmentId) {
		if (caseWorkerId == null) {
			return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "caseWorkerId is required"));
		}
		boolean allowedRole = role != null && (role.equalsIgnoreCase("DEPARTMENT_OFFICER")
				|| role.equalsIgnoreCase("SUPERVISORY_OFFICER")
				|| role.equalsIgnoreCase("ADMIN"));
		if (!allowedRole) {
			return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
		}

		if (isDepartmentRestrictedRole(role)) {
			if (requesterDepartmentId == null) {
				return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized for this department"));
			}
			return grievanceRepository.findByAssignedWokerId(caseWorkerId)
					.filter(grievance -> isSameDepartment(requesterDepartmentId, grievance.getDepartmentId()));
		}
		return grievanceRepository.findByAssignedWokerId(caseWorkerId);
	}

	// list grievances for the authenticated citizen
	public Flux<Grievance> getByCitizen(String citizenId) {
		if (citizenId == null) {
			return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
		}
		return grievanceRepository.findByCitizenId(citizenId);
	}

	// list distinct case workers assigned by the current department officer
	public Flux<String> getCaseWorkersForOfficer(String officerId, String role) {
		if (officerId == null) {
			return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
		}
		boolean allowedRole = role != null && (role.equalsIgnoreCase("DEPARTMENT_OFFICER")
				|| role.equalsIgnoreCase("SUPERVISORY_OFFICER")
				|| role.equalsIgnoreCase("ADMIN"));
		if (!allowedRole) {
			return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
		}
		return assignmentRepository.findByAssignedBy(officerId)
				.map(Assignment::getAssignedTo)
				.filter(id -> id != null)
				.distinct();
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
	
	private Mono<Grievance> ensureSameDepartmentForRestrictedRole(String role, String requesterDepartmentId, Grievance grievance) {
		if (isDepartmentRestrictedRole(role) && !isSameDepartment(requesterDepartmentId, grievance.getDepartmentId())) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized for this department"));
		}
		return Mono.just(grievance);
	}

	private boolean isDepartmentRestrictedRole(String role) {
		return role != null &&
				(role.equalsIgnoreCase("DEPARTMENT_OFFICER") || role.equalsIgnoreCase("CASE_WORKER"));
	}

	private boolean isSupervisoryOfficer(String role) {
		return role != null && role.equalsIgnoreCase("SUPERVISORY_OFFICER");
	}

	private boolean isCompletedStatus(GrievanceStatus status) {
		return status == GrievanceStatus.RESOLVED ||
				status == GrievanceStatus.CLOSED ||
				status == GrievanceStatus.WORK_DONE;
	}

	private Mono<Void> ensureCaseWorkerAvailable(String caseWorkerId) {
		return grievanceRepository.findByAssignedWokerId(caseWorkerId)
				.filter(grievance -> !isCompletedStatus(grievance.getStatus()))
				.hasElements()
				.flatMap(hasActive -> {
					if (hasActive) {
						return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
								"Case worker already has an active assignment"));
					}
					return Mono.empty();
				});
	}

	private boolean isSameDepartment(String requesterDepartmentId, String targetDepartmentId) {
		return requesterDepartmentId != null && targetDepartmentId != null
				&& requesterDepartmentId.equalsIgnoreCase(targetDepartmentId);
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
	                )
	                .flatMap(updated -> grievanceEventPublisher
	                        .publishStatusChange(updated, GrievanceStatus.ESCALATED,
	                                "We escalated your grievance for quicker attention")
	                        .thenReturn(updated));
	        });
	}


}
