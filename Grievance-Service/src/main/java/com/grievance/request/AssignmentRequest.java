package com.grievance.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotBlank(message = "Assigned To is required")
    private String assignedTo;  // Case Worker ID

	public String getGrievanceId() {
		return grievanceId;
	}

	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	
}
