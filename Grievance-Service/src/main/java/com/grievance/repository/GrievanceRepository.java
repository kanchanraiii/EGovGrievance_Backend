package com.grievance.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.Grievance;

public interface GrievanceRepository  extends ReactiveMongoRepository<Grievance, String> {

}
