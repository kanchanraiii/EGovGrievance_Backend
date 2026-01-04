package com.grievance.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grievance.model.GrievanceStatus;
import com.grievance.repository.GrievanceRepository;
import com.grievance.service.GrievanceService;

@Component
public class GrievanceScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(GrievanceScheduler.class);
	private final GrievanceRepository grievanceRepository;
	private final GrievanceService grievanceService;

	public GrievanceScheduler(GrievanceRepository grievanceRepository, GrievanceService grievanceService) {
		this.grievanceRepository = grievanceRepository;
		this.grievanceService = grievanceService;
	}
	
	// runs every 10 seconds (local test)
	 @Scheduled(fixedDelay = 10 * 1000)
	    public void checkSlaBreaches() {

	        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
	        log.info("SLA check started. threshold={}", threshold);

	        grievanceRepository
	            .findByStatusInAndAssignedAtBeforeAndEscalatedFalse(
	                List.of(
	                    GrievanceStatus.ASSIGNED,
	                    GrievanceStatus.IN_PROGRESS
	                ),
	                threshold
	            )
	            .doOnNext(grievance -> log.info("Escalating grievanceId={} status={} assignedAt={}",
	                    grievance.getId(), grievance.getStatus(), grievance.getAssignedAt()))
	            .flatMap(grievance ->
	                grievanceService.escalateGrievance(
	                    grievance.getId(),
	                    "SYSTEM"
	                )
	            )
	            .doOnNext(updated -> log.info("Escalated grievanceId={} status={} escalated={}",
	                    updated.getId(), updated.getStatus(), updated.isEscalated()))
	            .doOnError(error -> log.error("SLA escalation failed", error))
	            .subscribe();
	    }
	}
	
