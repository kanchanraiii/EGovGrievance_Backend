package com.storage.model;

import lombok.Data;

@Data
public class FileMetadata {

    private String id;
    private String grievanceId;  // grievance id ref
    private String fileName;
    private String contentType;
    private String uploadedBy;
	
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
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getUploadedBy() {
		return uploadedBy;
	}
	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	} 
    
    
}
