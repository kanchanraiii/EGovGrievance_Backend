package com.feedback.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReopenRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotBlank(message = "Reopen reason is required")
    private String reason;

    // getters and setters

    public String getGrievanceId() {
		return grievanceId;
	}

	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
    
       
}
