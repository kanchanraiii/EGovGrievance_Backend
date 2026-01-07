package com.grievance.request;

import com.grievance.model.Grievance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GrievanceCreateRequest {

    // Dept ID like water, electricity etc - Assigned Later
    private String departmentId;

    @NotBlank(message = "Category Code is required") // catergory code lets say is water
    private String categoryCode;

    @NotBlank(message = "Sub Category Code is required") // then subcategory within water - sanitation, corruption etc
    private String subCategoryCode;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

	public Grievance toGrievance() {
        Grievance grievance = new Grievance();
        grievance.setDepartmentId(departmentId);
        grievance.setCategoryCode(categoryCode);
        grievance.setSubCategoryCode(subCategoryCode);
        grievance.setDescription(description);
        return grievance;
    }
}
