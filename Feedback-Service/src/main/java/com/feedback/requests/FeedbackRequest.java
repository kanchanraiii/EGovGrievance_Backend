package com.feedback.requests;

import java.util.function.IntPredicate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotBlank(message = "Citizen ID is required")
    private String citizenId;

    @NotBlank(message = "Comments cannot be empty")
    @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
    private String comments;

	// getters and setters
    public String getGrievanceId() {
		return grievanceId;
	}

	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}

	public String getCitizenId() {
		return citizenId;
	}

	public void setCitizenId(String citizenId) {
		this.citizenId = citizenId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}


    
}
