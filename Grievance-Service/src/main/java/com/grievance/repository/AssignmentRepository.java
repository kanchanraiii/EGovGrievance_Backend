package com.grievance.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.Assignment;

public interface AssignmentRepository  extends ReactiveMongoRepository<Assignment, String> {

}
