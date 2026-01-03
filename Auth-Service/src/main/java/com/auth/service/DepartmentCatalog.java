package com.auth.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DepartmentCatalog {

    private static final Logger log = LoggerFactory.getLogger(DepartmentCatalog.class);

    private volatile Set<String> departmentIds;
    private volatile boolean hasDepartmentData;
    private final String departmentsFilePath;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public DepartmentCatalog(
            @Value("${departments.file:../departments-config/departments-db.json}") String departmentsFilePath,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper) {
        this.departmentsFilePath = departmentsFilePath;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.departmentIds = loadDepartmentIds(this.departmentsFilePath, this.resourceLoader, this.objectMapper);
        this.hasDepartmentData = !this.departmentIds.isEmpty();
    }

    public boolean isValid(String departmentId) {
        if (departmentId == null || departmentId.trim().isEmpty()) {
            return false;
        }
        if (!hasDepartmentData) {
            return false;
        }
        return departmentIds.contains(departmentId.trim().toUpperCase(Locale.ROOT));
    }

    public synchronized void refresh() {
        this.departmentIds = loadDepartmentIds(this.departmentsFilePath, this.resourceLoader, this.objectMapper);
        this.hasDepartmentData = !this.departmentIds.isEmpty();
    }

    private Set<String> loadDepartmentIds(String path, ResourceLoader loader, ObjectMapper mapper) {
        Resource resource = loader.getResource(resolvePath(path));
        if (!resource.exists()) {
            // Try classpath fallback when a filesystem path is absent
            Resource fallback = loader.getResource("classpath:departments-config/departments-db.json");
            if (fallback.exists()) {
                resource = fallback;
                log.info("Using classpath departments file departments-config/departments-db.json");
            } else {
                log.warn("Departments file not found at {} and no classpath fallback available", path);
                return Collections.emptySet();
            }
        }

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = mapper.readTree(is);
            Set<String> ids = new HashSet<>();
            collectIds(root.get("centralGovernmentDepartments"), ids);
            collectIds(root.get("stateGovernmentDepartments"), ids);
            log.info("Loaded {} department ids for validation", ids.size());
            return ids;
        } catch (IOException ex) {
            log.warn("Failed to read departments file {}: {}", path, ex.getMessage());
            return Collections.emptySet();
        }
    }

    private void collectIds(JsonNode arrayNode, Set<String> ids) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return;
        }
        arrayNode.forEach(node -> {
            JsonNode idNode = node.get("id");
            if (idNode != null && idNode.isTextual()) {
                ids.add(idNode.asText().trim().toUpperCase(Locale.ROOT));
            }
        });
    }

    private String resolvePath(String path) {
        // Support both classpath and file system paths without forcing callers to include the prefix
        if (path.startsWith("classpath:") || path.startsWith("file:")) {
            return path;
        }
        return "file:" + path;
    }
}
