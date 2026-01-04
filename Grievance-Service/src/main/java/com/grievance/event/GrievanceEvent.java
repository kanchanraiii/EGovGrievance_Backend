package com.grievance.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class GrievanceEvent {

    @EqualsAndHashCode.Include
    private String grievanceId;
    private String userId;     // recipient's id
    private String message;
    private String eventType;  // SUBMITTED / ASSIGNED / ESCALATED / RESOLVED
	
}
