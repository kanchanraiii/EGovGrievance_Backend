package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.Feedback;


public interface FeedbackRepository extends ReactiveMongoRepository<Feedback,String> {
	

}
