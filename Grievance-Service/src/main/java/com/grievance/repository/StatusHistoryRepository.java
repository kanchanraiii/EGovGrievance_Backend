package com.grievance.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.GrievanceHistory;

public interface StatusHistoryRepository  extends ReactiveMongoRepository<GrievanceHistory, String> {

}
