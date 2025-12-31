package com.feedback.client;

import lombok.Data;

@Data
public class GrievanceResponse {
	
	// response to check for id and status - RESOLVED / CLOSED
	
	private String id;
	private String status;
	
	// getters and setters
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean isResolvedOrClosed() {
	    return "RESOLVED".equals(status) || "CLOSED".equals(status);
	}

	
	

}
