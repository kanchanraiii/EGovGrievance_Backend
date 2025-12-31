package com.grievance.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grievance.model.GrievanceStatus;
import com.grievance.repository.GrievanceRepository;
import com.grievance.service.GrievanceService;

@Component
public class GrievanceScheduler {
	
	@Autowired
	private GrievanceRepository grievanceRepository;
	
	@Autowired
	private GrievanceService grievanceService;
	
	// runs every five minutes
	 @Scheduled(fixedDelay = 5*60* 1000)
	    public void checkSlaBreaches() {

	        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

	        grievanceRepository
	            .findByStatusInAndAssignedAtBeforeAndEscalatedFalse(
	                List.of(
	                    GrievanceStatus.ASSIGNED,
	                    GrievanceStatus.IN_PROGRESS
	                ),
	                threshold
	            )
	            .flatMap(grievance ->
	                grievanceService.escalateGrievance(
	                    grievance.getId(),
	                    "SYSTEM"
	                )
	            )
	            .subscribe();
	    }
	}
	

