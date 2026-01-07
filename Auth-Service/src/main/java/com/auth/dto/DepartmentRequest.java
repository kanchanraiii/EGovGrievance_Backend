package com.auth.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department id is required")
    private String id;

    @NotBlank(message = "Department name is required")
    private String name;

    @NotBlank(message = "Department level is required")
    private String level; // CENTRAL or STATE

    @Valid
    private List<CategoryRequest> categories;
}
