package com.grievance.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotBlank(message = "Assigned By is required")
    private String assignedBy;  // Department Officer ID

    @NotBlank(message = "Assigned To is required")
    private String assignedTo;  // Case Worker ID

	public String getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	
}
