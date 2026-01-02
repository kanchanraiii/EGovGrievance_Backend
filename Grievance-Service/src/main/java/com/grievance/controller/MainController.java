package com.grievance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.request.AssignmentRequest;
import com.grievance.request.GrievanceCreateRequest;
import com.grievance.request.StatusUpdateRequest;
import com.grievance.service.GrievanceService;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("api/grievances")
public class MainController {

	@Autowired
	private GrievanceService grievanceService;

	// create a grievance
	@PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Map<String, String>> createGrievance(@Valid @RequestBody GrievanceCreateRequest request) {
		return grievanceService.createGrievance(request)
				.map(saved -> Map.of("grievanceId", saved.getId()));
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
	public Flux<Grievance> getAllGrievances() {
		return grievanceService.getAll();
	}

	// to assign a grievance
	@PatchMapping("/assign")
	public Mono<Map<String, String>> assignGrievance(@Valid @RequestBody AssignmentRequest request) {
		return grievanceService
				.assignGrievance(request.getGrievanceId(), request.getAssignedBy(), request.getAssignedTo())
				.map(updated -> Map.of(
						"status", updated.getStatus().name(),
						"assignedTo", updated.getAssignedWokerId()));
	}

	// to update status
	@PatchMapping("/status")
	public Mono<Grievance> updateStatus(@Valid @RequestBody StatusUpdateRequest request) {
		return null;
	}

	// to get status history of grievances
	@GetMapping("/history/{id}")
	public Flux<GrievanceHistory> getStatusHistory(@PathVariable String id) {

		return grievanceService.getStatusHistory(id);
	}

}
