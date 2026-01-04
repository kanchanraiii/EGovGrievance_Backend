package com.auth.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category code is required")
    private String code;

    @NotBlank(message = "Category name is required")
    private String name;

    @Valid
    private List<SubCategoryRequest> subCategories;

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

	public List<SubCategoryRequest> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<SubCategoryRequest> subCategories) {
		this.subCategories = subCategories;
	}

	
}
