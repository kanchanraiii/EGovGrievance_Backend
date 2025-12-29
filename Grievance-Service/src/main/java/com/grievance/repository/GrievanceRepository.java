package com.grievance.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.Grievance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GrievanceRepository  extends ReactiveMongoRepository<Grievance, String> {
	
	Flux<Grievance> findByDepartmentId(String departmentId);

    Flux<Grievance> findByCitizenId(String citizenId);

    Flux<Grievance> findByStatus(String status);

	Mono<Grievance> findById(ObjectId objectId);

}
