package com.grievance.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Document(collection="grievance")
public class Grievance {
	
	@Id
	@EqualsAndHashCode.Include
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
    
	
	private GrievanceStatus status;
    
	private LocalDateTime createdAt;
    
	private LocalDateTime updatedAt;
	
	// SLA reated
	private LocalDateTime assignedAt;
	private boolean escalated;
	
	// getters and setters
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public LocalDateTime getAssignedAt() {
		return assignedAt;
	}
	public void setAssignedAt(LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}
	public boolean isEscalated() {
		return escalated;
	}
	public void setEscalated(boolean escalated) {
		this.escalated = escalated;
	}
	
	
		
}
