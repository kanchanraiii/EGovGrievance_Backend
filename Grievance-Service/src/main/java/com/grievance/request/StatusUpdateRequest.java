package com.grievance.request;

import com.grievance.model.GrievanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required") // enum
    private GrievanceStatus status;

    @NotBlank(message = "Updated by user ID is required")
    private String updatedBy;   // citizen / DO / case worker

    private String remarks;

	public GrievanceStatus getStatus() {
		return status;
	}

	public void setStatus(GrievanceStatus status) {
		this.status = status;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
    
    
}
