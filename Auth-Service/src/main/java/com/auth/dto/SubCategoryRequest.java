package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubCategoryRequest {

    @NotBlank(message = "Sub-category code is required")
    private String code;

    @NotBlank(message = "Sub-category name is required")
    private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
