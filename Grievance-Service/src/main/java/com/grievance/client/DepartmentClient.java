package com.grievance.client;

import com.grievance.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DepartmentClient {

    private final WebClient webClient;

    // connecting to json server
    public DepartmentClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:3000").build();
    }

    // to check if a dept is valid or exists
    public Mono<Boolean> isValidDepartment(
            String departmentCode,
            String categoryCode,
            String subCategoryCode) {

        if (departmentCode == null || categoryCode == null || subCategoryCode == null) {
            return Mono.just(false);
        }

        return Flux.concat(
                        fetchDepartments("/centralGovernmentDepartments"),
                        fetchDepartments("/stateGovernmentDepartments"))
                .filter(department -> departmentCode.equalsIgnoreCase(department.getId()))
                .next()
                .map(department -> hasCategoryAndSubCategory(department, categoryCode, subCategoryCode))
                .defaultIfEmpty(false)
                .onErrorMap(ex -> new ServiceException("Department JSON Server unavailable"));
    }

    // fetch dept
    private Flux<DepartmentResponse> fetchDepartments(String path) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToFlux(DepartmentResponse.class);
    }

    // check for category and subcategory
    private boolean hasCategoryAndSubCategory(
            DepartmentResponse department,
            String categoryCode,
            String subCategoryCode) {

        if (department.getCategories() == null) {
            return false;
        }

        return department.getCategories().stream()
                .filter(category -> categoryCode.equalsIgnoreCase(category.getCode()))
                .anyMatch(category -> category.getSubCategories() != null &&
                        category.getSubCategories().stream()
                                .anyMatch(sub -> subCategoryCode.equalsIgnoreCase(sub.getCode())));
    }
}
