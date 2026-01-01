package com.grievance.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Data
@Document(collection = "assignments")
public class Assignment {

    @Id
    private String id;

    @NotBlank(message="Grievance ID is required")
    private String grievanceId;

    @NotBlank(message="Assigned By is required")
    private String assignedBy;  // Department Officer ID   

    @NotBlank(message="Assigned To is required")
    private String assignedTo;  // Case Worker ID 

    private LocalDateTime assignedAt;
}
