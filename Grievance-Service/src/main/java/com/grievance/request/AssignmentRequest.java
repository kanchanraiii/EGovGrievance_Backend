package com.grievance.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotBlank(message = "Assigned By is required")
    private String assignedBy;  // Department Officer ID

    @NotBlank(message = "Assigned To is required")
    private String assignedTo;  // Case Worker ID
}
