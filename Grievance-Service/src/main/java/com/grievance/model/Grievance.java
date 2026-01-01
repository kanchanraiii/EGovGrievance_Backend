package com.grievance.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Document(collection="grievance")
public class Grievance {
	
	@Id
	private String id;
	
	@NotBlank(message="Citizen ID is required")
	private String citizenId;
	
	// Dept ID like water, electricity etc - Assigned Later
	private String departmentId;
	
	// Assigned Later - Case Worker ID
	private String assignedWokerId;
	
	@NotBlank(message="Category Code is required") // catergory code lets say is water
	private String categoryCode;
	
	@NotBlank(message="Sub Category Code is required") // then subcategory within water - sanitation, corruption etc 
	private String subCategoryCode;
	
	@NotBlank(message="Description is required")
	@Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
	private String description;
    
	// Set in Service 
	private GrievanceStatus status;
    
	private LocalDateTime createdAt;
    
	private LocalDateTime updatedAt;
	
	// SLA reated
	private LocalDateTime assignedAt;
	private boolean escalated;
		
}
