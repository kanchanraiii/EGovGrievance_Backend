package com.grievance.client;

import com.grievance.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DepartmentClient {

    private final WebClient webClient;
    private final String departmentsPath;

    // connecting to department service (Auth Service via gateway or direct)
    public DepartmentClient(
            WebClient.Builder builder,
            @Value("${departments.service.base-url:http://localhost:9007}") String baseUrl,
            @Value("${departments.service.path:/api/auth/departments}") String departmentsPath) {
        this.webClient = builder.baseUrl(trimTrailingSlash(baseUrl)).build();
        this.departmentsPath = ensureLeadingSlash(departmentsPath);
    }

    // to check if a dept is valid or exists
    public Mono<Boolean> isValidDepartment(
            String departmentCode,
            String categoryCode,
            String subCategoryCode) {

        if (departmentCode == null || categoryCode == null || subCategoryCode == null) {
            return Mono.just(false);
        }

        return fetchAllDepartments()
                .flatMapMany(this::flattenDepartments)
                .filter(department -> departmentCode.equalsIgnoreCase(department.getId()))
                .next()
                .map(department -> hasCategoryAndSubCategory(department, categoryCode, subCategoryCode))
                .defaultIfEmpty(false)
                .onErrorMap(ex -> new ServiceException("Department service unavailable"));
    }

    // fetch dept
    private Mono<DepartmentsResponse> fetchAllDepartments() {
        return webClient.get()
                .uri(departmentsPath)
                .retrieve()
                .bodyToMono(DepartmentsResponse.class);
    }

    private Flux<DepartmentResponse> flattenDepartments(DepartmentsResponse response) {
        return Flux.concat(
                Flux.fromIterable(nullSafe(response.getCentralGovernmentDepartments())),
                Flux.fromIterable(nullSafe(response.getStateGovernmentDepartments()))
        );
    }

    private List<DepartmentResponse> nullSafe(List<DepartmentResponse> departments) {
        return departments == null ? List.of() : departments;
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

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String ensureLeadingSlash(String path) {
        if (path == null || path.isBlank()) {
            return "/api/auth/departments";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
