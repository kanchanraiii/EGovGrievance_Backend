package com.feedback.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingsRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;  // grievance id ref

    @NotNull(message = "Rating score is required")
    @Min(value = 1, message = "Rating must be at least 1") // setting min value
    @Max(value = 5, message = "Rating cannot exceed 5") // setting max value
    private Integer score;

    // getters and setters
    public String getGrievanceId() {
		return grievanceId;
	}

	public void setGrievanceId(String grievanceId) {
		this.grievanceId = grievanceId;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}
    
}
