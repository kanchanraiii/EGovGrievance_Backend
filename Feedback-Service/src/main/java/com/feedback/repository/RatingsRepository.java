package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.Ratings;

public interface RatingsRepository extends ReactiveMongoRepository<Ratings, String>{

}
