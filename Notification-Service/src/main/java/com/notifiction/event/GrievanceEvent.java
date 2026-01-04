package com.notifiction.event;

import lombok.Data;

@Data
public class GrievanceEvent {

    private String grievanceId;
    private String userId;     // recipient's id
    private String message;
    private String eventType;  // SUBMITTED / ASSIGNED / ESCALATED / RESOLVED
	
}
