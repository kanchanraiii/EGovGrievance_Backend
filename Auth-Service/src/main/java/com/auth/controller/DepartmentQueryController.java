package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.auth.service.DepartmentConfigService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth/departments")
public class DepartmentQueryController {

    private final DepartmentConfigService departmentConfigService;

    @Autowired
    public DepartmentQueryController(DepartmentConfigService departmentConfigService) {
        this.departmentConfigService = departmentConfigService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<ObjectNode> getAllDepartments() {
        return departmentConfigService.getAllDepartments();
    }

    @GetMapping("/{departmentId}/categories")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ArrayNode> getCategories(@PathVariable String departmentId) {
        return departmentConfigService.getCategories(departmentId);
    }
}
