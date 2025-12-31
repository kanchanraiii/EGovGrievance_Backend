package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.ReopenRequest;
import reactor.core.publisher.Mono;

public interface ReopenRequestRepository extends ReactiveMongoRepository<ReopenRequest,String> {
	
	Mono<Boolean> existsByGrievanceId(String grievanceId);

}
