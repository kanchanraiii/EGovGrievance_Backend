package com.grievance.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.grievance.model.GrievanceHistory;

import reactor.core.publisher.Flux;

public interface StatusHistoryRepository  extends ReactiveMongoRepository<GrievanceHistory, String> {

	Flux<GrievanceHistory> findByGrievanceIdOrderByUpdatedAtAsc(String grievanceId);

}
