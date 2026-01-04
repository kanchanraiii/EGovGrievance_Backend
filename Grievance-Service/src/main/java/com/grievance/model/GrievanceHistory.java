package com.grievance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Document(collection = "status_history")
public class GrievanceHistory {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotNull(message = "Status is required") // enum
    private GrievanceStatus status; 

    @NotBlank(message = "Updated by user ID is required")
    private String updatedBy;   // citizen / DO / case worker

    private String remarks;

    private LocalDateTime updatedAt;

	
	// getters and setters
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGrievanceId() {
		return grievanceId;
	}

	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}

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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}


    
    

}
