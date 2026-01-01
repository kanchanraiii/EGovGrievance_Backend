package com.grievance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "status_history")
public class GrievanceHistory {

    @Id
    private String id;

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotNull(message = "Status is required") // enum
    private GrievanceStatus status; 

    @NotBlank(message = "Updated by user ID is required")
    private String updatedBy;   // citizen / DO / case worker

    private String remarks;

    private LocalDateTime updatedAt;
}
