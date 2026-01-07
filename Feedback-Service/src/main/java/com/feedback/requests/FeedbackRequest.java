package com.feedback.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FeedbackRequest {

    @NotBlank(message = "Grievance ID is required")
    private String grievanceId;

    @NotBlank(message = "Comments cannot be empty")
    @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
    private String comments;

}
