package com.grievance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import com.grievance.client.AuthClient;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.EscalatedGrievanceView;
import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;
import com.grievance.service.GrievanceService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("api/grievances")
public class MainController {

	private static final String CLAIM_DEPARTMENT_ID = "departmentId";

	private final GrievanceService grievanceService;
	private final AuthClient authClient;

	public MainController(GrievanceService grievanceService, AuthClient authClient) {
		this.grievanceService = grievanceService;
		this.authClient = authClient;
	}

	// create a grievance
	@PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Map<String, String>> createGrievance(@Valid @RequestBody GrievanceCreateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String emailClaim = jwt.getClaim("email");
		Mono<String> emailMono = StringUtils.hasText(emailClaim)
				? Mono.just(emailClaim)
				: authClient.fetchEmail(jwt.getTokenValue()).defaultIfEmpty(null);

		return emailMono.flatMap(email ->
				grievanceService.createGrievance(request, jwt.getSubject(), email)
						.map(saved -> Map.of("grievanceId", saved.getId()))
		);
	}

	// get grievance by id
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Mono<Grievance> getGrievanceById(@PathVariable String id) {
		return grievanceService.getById(id);

	}

	// get all grievances
	@GetMapping("/getAll")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Grievance> getAllGrievances(@AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getAllForRole(jwt.getClaim("role"), jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}

	// to assign a grievance
	@PatchMapping("/assign")
	public Mono<Map<String, String>> assignGrievance(@Valid @RequestBody AssignmentRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String actorId = jwt.getSubject();
		return grievanceService
				.assignGrievance(request.getGrievanceId(), actorId, request.getAssignedTo(), jwt.getClaim("role"),
						jwt.getClaim(CLAIM_DEPARTMENT_ID))
				.map(updated -> Map.of(
						"status", updated.getStatus().name(),
						"assignedTo", updated.getAssignedWokerId()));
	}

	// to update status
	@PatchMapping("/status")
	public Mono<Grievance> updateStatus(@Valid @RequestBody StatusUpdateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String actorId = jwt.getSubject();
		return grievanceService.updateStatus(
				request.getGrievanceId(),
				request.getStatus(),
				actorId,
				request.getRemarks(),
				jwt.getClaim("role"),
				jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}

	// to get status history of grievances
	@GetMapping("/history/{id}")
	public Flux<GrievanceHistory> getStatusHistory(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getStatusHistory(id, jwt.getClaim("role"), jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}

	// view grievances by department (department officer / case worker / admin)
	@GetMapping("/department/{departmentId}")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Grievance> getByDepartment(@PathVariable String departmentId, @AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getByDepartment(departmentId, jwt.getClaim("role"), jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}

	// view grievances assigned to a specific case worker
	@GetMapping("/case-worker/{caseWorkerId}")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Grievance> getByCaseWorker(@PathVariable String caseWorkerId, @AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getByCaseWorker(caseWorkerId, jwt.getClaim("role"), jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}

	// view grievances for current citizen
	@GetMapping("/my")
	@ResponseStatus(HttpStatus.OK)
	public Flux<Grievance> getMyGrievances(@AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getByCitizen(jwt.getSubject());
	}

	// view case-workers in a department
	@GetMapping("/my-case-workers")
	@ResponseStatus(HttpStatus.OK)
	public Mono<Map<String, List<String>>> getMyCaseWorkers(@AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getCaseWorkersForOfficer(jwt.getSubject(), jwt.getClaim("role"))
				.collectList()
				.map(workers -> Map.of("caseWorkers", workers));
	}

    // view grievances assigned to CURRENT case worker
    @GetMapping("/my-assigned")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Grievance> getMyAssignedGrievances(@AuthenticationPrincipal Jwt jwt) {

        String role = jwt.getClaim("role");
        String caseWorkerId = jwt.getSubject();

        if (!"CASE_WORKER".equals(role)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only case workers can access this endpoint");
        }

        return grievanceService.getByCaseWorkerSelf(
                caseWorkerId,
                jwt.getClaim("email"),
                jwt.getClaim(CLAIM_DEPARTMENT_ID));
    }
	
	// get ALL case workers in my department
	@GetMapping("/department/case-workers")
	@ResponseStatus(HttpStatus.OK)
	public Mono<Map<String, List<String>>> getAllCaseWorkersInDepartment(
	        @AuthenticationPrincipal Jwt jwt) {

	    return grievanceService
	            .getAllCaseWorkersInDepartment(
	                    jwt.getClaim("role"),
	                    jwt.getClaim(CLAIM_DEPARTMENT_ID)
	            )
	            .collectList()
	            .map(workers -> Map.of("caseWorkers", workers));
	}

	// view escalated grievances (SO/Admin)
	@GetMapping("/escalated")
	@ResponseStatus(HttpStatus.OK)
	public Flux<EscalatedGrievanceView> getEscalated(@AuthenticationPrincipal Jwt jwt) {
		return grievanceService.getEscalatedForSupervisor(
				jwt.getClaim("role"),
				jwt.getClaim(CLAIM_DEPARTMENT_ID));
	}



}
