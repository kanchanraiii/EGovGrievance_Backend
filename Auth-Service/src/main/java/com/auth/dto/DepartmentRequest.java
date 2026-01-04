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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public List<CategoryRequest> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryRequest> categories) {
		this.categories = categories;
	}

	
}
