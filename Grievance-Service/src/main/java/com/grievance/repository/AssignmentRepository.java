package com.grievance.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.Assignment;
import reactor.core.publisher.Flux;

public interface AssignmentRepository  extends ReactiveMongoRepository<Assignment, String> {
	 Flux<Assignment> findByGrievanceId(String grievanceId);

}
