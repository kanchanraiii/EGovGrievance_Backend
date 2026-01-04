package com.grievance.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GrievanceRepository  extends ReactiveMongoRepository<Grievance, String> {
	
	Flux<Grievance> findByDepartmentId(String departmentId);

    Flux<Grievance> findByCitizenId(String citizenId);

    Flux<Grievance> findByStatus(String status);
    Flux<Grievance> findByStatus(com.grievance.model.GrievanceStatus status);
	
	Mono<Grievance> findById(ObjectId objectId);

	Flux<Grievance> findByAssignedWokerId(String assignedWokerId);
	
	// for SLA
	Flux<Grievance> findByStatusInAndAssignedAtBeforeAndEscalatedFalse(
	        List<GrievanceStatus> statuses,
	        LocalDateTime time
	    );

}
