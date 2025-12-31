package com.feedback.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.feedback.model.ReopenRequest;

public interface ReopenRequestRepository extends ReactiveMongoRepository<ReopenRequest,String> {

}
