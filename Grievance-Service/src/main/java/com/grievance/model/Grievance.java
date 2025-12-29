package com.grievance.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Document(collection="grievance")
public class Grievance {
	
	@Id
	private String Id;
	
	@NotBlank(message="Citizen ID is required")
	private String citizenId;
	
	@NotBlank(message="Department ID is required")  // Dept ID like water, electricity etc
	private String departmentId;
	
	@NotBlank(message="Assigned Worker ID is required")  // Case Worker ID
	private String assignedWokerId;
	
	@NotBlank(message="Category Code is required") // catergory code lets say is what
	private String categoryCode;
	
	@NotBlank(message="Sub Category Code is required") // then subcategory within water - sanitation, corruption etc 
	private String subCategoryCode;
	
	@NotBlank(message="Description is required")
	@Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
	private String description;
    
	@NotNull(message = "Status is required") // enums
	private GrievanceStatus status;
    
	private String priority;
    
	private LocalDateTime createdAt;
    
	private LocalDateTime updatedAt;

	
	// getters and setters
	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getCitizenId() {
		return citizenId;
	}

	public void setCitizenId(String citizenId) {
		this.citizenId = citizenId;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public String getAssignedWokerId() {
		return assignedWokerId;
	}

	public void setAssignedWokerId(String assignedWokerId) {
		this.assignedWokerId = assignedWokerId;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getSubCategoryCode() {
		return subCategoryCode;
	}

	public void setSubCategoryCode(String subCategoryCode) {
		this.subCategoryCode = subCategoryCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GrievanceStatus getStatus() {
		return status;
	}

	public void setStatus(GrievanceStatus status) {
		this.status = status;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
		
}
