package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.Feedback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface FeedbackRepository extends ReactiveMongoRepository<Feedback,String> {
	
	Flux<Feedback> findByGrievanceId(String grievanceId);
	
	Mono<Boolean> existsByGrievanceIdAndCitizenId(String grievanceId, String citizenId);
	
	Mono<Boolean> existsByGrievanceId(String grievanceId);
	
}
