package com.grievance.model;

public class EscalatedGrievanceView {

    private String grievanceId;
    private String description;
    private String departmentId;
    private String assignedTo;
    private String assignedBy;
    private GrievanceStatus status;

    public EscalatedGrievanceView() {
    }

    public EscalatedGrievanceView(
            String grievanceId,
            String description,
            String departmentId,
            String assignedTo,
            String assignedBy,
            GrievanceStatus status) {
        this.grievanceId = grievanceId;
        this.description = description;
        this.departmentId = departmentId;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.status = status;
    }

    public String getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(String grievanceId) {
        this.grievanceId = grievanceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public GrievanceStatus getStatus() {
        return status;
    }

    public void setStatus(GrievanceStatus status) {
        this.status = status;
    }
}
