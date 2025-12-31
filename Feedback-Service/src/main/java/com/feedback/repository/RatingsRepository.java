package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.Ratings;
import reactor.core.publisher.Mono;

public interface RatingsRepository extends ReactiveMongoRepository<Ratings, String>{
	
	Mono<Boolean> existsByGrievanceId(String grievanceId);

}
