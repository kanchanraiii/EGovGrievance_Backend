package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.CategoryRequest;
import com.auth.dto.DepartmentRequest;
import com.auth.service.DepartmentConfigService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth/admin/departments")
public class AdminDepartmentController {

    private final DepartmentConfigService departmentConfigService;

    @Autowired
    public AdminDepartmentController(DepartmentConfigService departmentConfigService) {
        this.departmentConfigService = departmentConfigService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addDepartment(@Valid @RequestBody DepartmentRequest request) {
        return departmentConfigService.addDepartment(request);
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDepartment(@PathVariable String departmentId) {
        return departmentConfigService.deleteDepartment(departmentId);
    }

    @PostMapping("/{departmentId}/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addCategory(@PathVariable String departmentId, @Valid @RequestBody CategoryRequest request) {
        return departmentConfigService.addCategory(departmentId, request);
    }

    @DeleteMapping("/{departmentId}/categories/{categoryCode}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCategory(@PathVariable String departmentId, @PathVariable String categoryCode) {
        return departmentConfigService.deleteCategory(departmentId, categoryCode);
    }
}
