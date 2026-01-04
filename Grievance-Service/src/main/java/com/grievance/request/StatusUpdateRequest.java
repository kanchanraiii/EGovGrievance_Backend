package com.grievance.request;

import com.grievance.model.GrievanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotNull(message = "Status is required") // enum
    private GrievanceStatus status;

    @NotBlank(message = "Updated by user ID is required")
    private String updatedBy;   // citizen / DO / case worker

    private String remarks;

}
