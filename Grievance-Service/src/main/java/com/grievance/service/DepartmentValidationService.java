package com.grievance.service;

import com.grievance.client.DepartmentClient;
import com.grievance.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DepartmentValidationService {

    private final DepartmentClient departmentClient;

    public DepartmentValidationService(DepartmentClient departmentClient) {
        this.departmentClient = departmentClient;
    }

    // validating departments
    public Mono<Void> validateDepartment(
            String departmentCode,
            String categoryCode,
            String subCategoryCode) {

        return departmentClient
                .isValidDepartment(departmentCode, categoryCode, subCategoryCode)
                .flatMap(valid -> {
                    if (!Boolean.TRUE.equals(valid)) {
                        return Mono.error(
                                new ResourceNotFoundException(
                                        "Department / category / sub-category not found"));
                    }
                    return Mono.empty();
                });
    }
}
