package com.grievance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceHistory;
import com.grievance.model.GrievanceStatus;
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
	public Mono<Grievance> createGrievance(@Valid @RequestBody Grievance grievance) {
		return grievanceService.createGrievance(grievance);
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
	@PatchMapping("/{id}/assign")
	public Mono<Grievance> assignGrievance(@PathVariable String id, @RequestParam String assignedBy,
			@RequestParam String assignedTo) {

		return grievanceService.assignGrievance(id, assignedBy, assignedTo);
	}

	// to update status
	@PatchMapping("/{id}/status")
	public Mono<Grievance> updateStatus(@PathVariable String id, @RequestParam GrievanceStatus status,
			@RequestParam String updatedBy, @RequestParam(required = false) String remarks) {

		return grievanceService.updateStatus(id, status, updatedBy, remarks);
	}

	// to get status history of grievances
	@GetMapping("/{id}/history")
	public Flux<GrievanceHistory> getStatusHistory(@PathVariable String id) {

		return grievanceService.getStatusHistory(id);
	}

}
