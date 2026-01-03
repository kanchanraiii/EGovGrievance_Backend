package com.auth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.CategoryRequest;
import com.auth.dto.DepartmentRequest;
import com.auth.dto.SubCategoryRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service
public class DepartmentConfigService {

    private final String departmentsFilePath;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final DepartmentCatalog departmentCatalog;

    public DepartmentConfigService(
            @Value("${departments.file:../departments-config/departments-db.json}") String departmentsFilePath,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            DepartmentCatalog departmentCatalog) {
        this.departmentsFilePath = departmentsFilePath;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.departmentCatalog = departmentCatalog;
    }

    public Mono<Void> addDepartment(DepartmentRequest request) {
        return Mono.fromRunnable(() -> addDepartmentBlocking(request));
    }

    public Mono<Void> deleteDepartment(String departmentId) {
        return Mono.fromRunnable(() -> deleteDepartmentBlocking(departmentId));
    }

    public Mono<Void> addCategory(String departmentId, CategoryRequest request) {
        return Mono.fromRunnable(() -> addCategoryBlocking(departmentId, request));
    }

    public Mono<Void> deleteCategory(String departmentId, String categoryCode) {
        return Mono.fromRunnable(() -> deleteCategoryBlocking(departmentId, categoryCode));
    }

    public Mono<ObjectNode> getAllDepartments() {
        return Mono.fromCallable(this::readRoot);
    }

    public Mono<ArrayNode> getCategories(String departmentId) {
        return Mono.fromCallable(() -> getCategoriesBlocking(departmentId));
    }

    private void addDepartmentBlocking(DepartmentRequest request) {
        ObjectNode root = readRoot();
        String departmentId = normalizeId(request.getId());

        if (findDepartment(root, departmentId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");
        }

        ArrayNode target = selectArray(root, request.getLevel());
        ObjectNode newDepartment = objectMapper.createObjectNode();
        newDepartment.put("id", departmentId);
        newDepartment.put("name", request.getName().trim());
        newDepartment.put("level", normalizeLevel(request.getLevel()));

        ArrayNode categoriesNode = objectMapper.createArrayNode();
        if (request.getCategories() != null) {
            request.getCategories()
                    .forEach(category -> categoriesNode.add(buildCategoryNode(category)));
        }
        newDepartment.set("categories", categoriesNode);

        target.add(newDepartment);
        writeRoot(root);
        departmentCatalog.refresh();
    }

    private void deleteDepartmentBlocking(String departmentIdRaw) {
        ObjectNode root = readRoot();
        String departmentId = normalizeId(departmentIdRaw);

        boolean removed = removeFromArray(root.withArray("centralGovernmentDepartments"), departmentId)
                || removeFromArray(root.withArray("stateGovernmentDepartments"), departmentId);

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }

        writeRoot(root);
        departmentCatalog.refresh();
    }

    private void addCategoryBlocking(String departmentIdRaw, CategoryRequest request) {
        ObjectNode root = readRoot();
        String departmentId = normalizeId(departmentIdRaw);
        String categoryCode = normalizeCode(request.getCode());

        ObjectNode department = findDepartment(root, departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        ArrayNode categories = department.withArray("categories");
        boolean exists = stream(categories)
                .anyMatch(node -> categoryCode.equalsIgnoreCase(node.path("code").asText()));

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        categories.add(buildCategoryNode(request, categoryCode));
        writeRoot(root);
    }

    private void deleteCategoryBlocking(String departmentIdRaw, String categoryCodeRaw) {
        ObjectNode root = readRoot();
        String departmentId = normalizeId(departmentIdRaw);
        String categoryCode = normalizeCode(categoryCodeRaw);

        ObjectNode department = findDepartment(root, departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        ArrayNode categories = department.withArray("categories");
        boolean removed = removeCategory(categories, categoryCode);

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        writeRoot(root);
    }

    private ArrayNode getCategoriesBlocking(String departmentIdRaw) {
        ObjectNode root = readRoot();
        String departmentId = normalizeId(departmentIdRaw);

        ObjectNode department = findDepartment(root, departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        return department.withArray("categories");
    }

    private ObjectNode readRoot() {
        Path path = resolveFilePath();
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                ObjectNode newRoot = objectMapper.createObjectNode();
                newRoot.set("centralGovernmentDepartments", objectMapper.createArrayNode());
                newRoot.set("stateGovernmentDepartments", objectMapper.createArrayNode());
                writeRoot(newRoot);
                return newRoot;
            } catch (IOException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to initialize departments file", ex);
            }
        }

        try {
            JsonNode node = objectMapper.readTree(path.toFile());
            if (node instanceof ObjectNode objectNode) {
                ensureArrays(objectNode);
                return objectNode;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid departments file structure");
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read departments file", ex);
        }
    }

    private void writeRoot(ObjectNode root) {
        Path path = resolveFilePath();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write departments file", ex);
        }
    }

    private Path resolveFilePath() {
        String path = departmentsFilePath;
        if (path.startsWith("classpath:")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Departments file is packaged in classpath and cannot be modified");
        }
        if (path.startsWith("file:")) {
            path = path.substring("file:".length());
        }
        return Paths.get(path).toAbsolutePath();
    }

    private Optional<ObjectNode> findDepartment(ObjectNode root, String departmentId) {
        return stream(root.withArray("centralGovernmentDepartments"))
                .filter(node -> departmentId.equalsIgnoreCase(node.path("id").asText()))
                .findFirst()
                .map(node -> (ObjectNode) node)
                .or(() -> stream(root.withArray("stateGovernmentDepartments"))
                        .filter(node -> departmentId.equalsIgnoreCase(node.path("id").asText()))
                        .findFirst()
                        .map(node -> (ObjectNode) node));
    }

    private ArrayNode selectArray(ObjectNode root, String levelRaw) {
        String level = normalizeLevel(levelRaw);
        if ("STATE".equalsIgnoreCase(level)) {
            return root.withArray("stateGovernmentDepartments");
        }
        return root.withArray("centralGovernmentDepartments");
    }

    private String normalizeId(String id) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department id is required");
        }
        return id.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Level is required");
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("CENTRAL") && !normalized.equals("STATE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Level must be CENTRAL or STATE");
        }
        return normalized;
    }

    private ObjectNode buildCategoryNode(CategoryRequest request) {
        return buildCategoryNode(request, normalizeCode(request.getCode()));
    }

    private ObjectNode buildCategoryNode(CategoryRequest request, String categoryCode) {
        ObjectNode category = objectMapper.createObjectNode();
        category.put("code", categoryCode);
        category.put("name", request.getName().trim());

        ArrayNode subCategories = objectMapper.createArrayNode();
        if (request.getSubCategories() != null) {
            for (SubCategoryRequest sub : request.getSubCategories()) {
                ObjectNode subNode = objectMapper.createObjectNode();
                subNode.put("code", normalizeCode(sub.getCode()));
                subNode.put("name", sub.getName().trim());
                subCategories.add(subNode);
            }
        }
        category.set("subCategories", subCategories);
        return category;
    }

    private boolean removeFromArray(ArrayNode array, String departmentId) {
        for (int i = 0; i < array.size(); i++) {
            JsonNode node = array.get(i);
            if (departmentId.equalsIgnoreCase(node.path("id").asText())) {
                array.remove(i);
                return true;
            }
        }
        return false;
    }

    private boolean removeCategory(ArrayNode categories, String categoryCode) {
        for (int i = 0; i < categories.size(); i++) {
            JsonNode node = categories.get(i);
            if (categoryCode.equalsIgnoreCase(node.path("code").asText())) {
                categories.remove(i);
                return true;
            }
        }
        return false;
    }

    private void ensureArrays(ObjectNode root) {
        root.withArray("centralGovernmentDepartments");
        root.withArray("stateGovernmentDepartments");
    }

    private Stream<JsonNode> stream(ArrayNode arrayNode) {
        return StreamSupport.stream(arrayNode.spliterator(), false);
    }
}
