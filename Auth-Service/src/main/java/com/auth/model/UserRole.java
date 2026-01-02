package com.auth.model;

// enum to identify different roles
public enum UserRole {
    CITIZEN,  // end user
    CASE_WORKER, // comes under department
    ADMIN, // root user with all privileges
    SUPERVISORY_OFFICER, // handles various departments
    DEPARTMENT_OFFICER; // head of a department

    public String value() {
        return name();
    }

    public boolean matches(String role) {
        return name().equalsIgnoreCase(role);
    }
}
