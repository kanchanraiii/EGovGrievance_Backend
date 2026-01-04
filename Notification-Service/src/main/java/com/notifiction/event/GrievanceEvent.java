package com.notifiction.event;

import lombok.Data;

@Data
public class GrievanceEvent {

    private String grievanceId;
    private String userId;     // recipient's id
    private String message;
    private String eventType;  // SUBMITTED / ASSIGNED / ESCALATED / RESOLVED
	
    public String getGrievanceId() {
		return grievanceId;
	}
	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
    
    
	
}
