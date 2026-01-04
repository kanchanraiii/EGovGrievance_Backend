package com.feedback.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection="feedback")
@NoArgsConstructor
public class Feedback {
	
	@Id
	private String id;
	
	@NotBlank(message="Grievance Id is required")  // greivance id ref from client
	private String grievanceId;
	
	@NotBlank(message="Citizen Id is required") // citizen id ref from client
	private String citizenId;
	
	@NotBlank(message="Comments cannot be empty")
	@Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
	private String comments;
	
	private LocalDateTime submittedAt;

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

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	
	
	
}
