package com.grievance.client;

import java.util.List;

public class DepartmentsResponse {

    private List<DepartmentResponse> centralGovernmentDepartments;
    private List<DepartmentResponse> stateGovernmentDepartments;

    public List<DepartmentResponse> getCentralGovernmentDepartments() {
        return centralGovernmentDepartments;
    }

    public void setCentralGovernmentDepartments(List<DepartmentResponse> centralGovernmentDepartments) {
        this.centralGovernmentDepartments = centralGovernmentDepartments;
    }

    public List<DepartmentResponse> getStateGovernmentDepartments() {
        return stateGovernmentDepartments;
    }

    public void setStateGovernmentDepartments(List<DepartmentResponse> stateGovernmentDepartments) {
        this.stateGovernmentDepartments = stateGovernmentDepartments;
    }
}
