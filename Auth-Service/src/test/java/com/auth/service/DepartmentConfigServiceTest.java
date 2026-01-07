package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.CategoryRequest;
import com.auth.dto.DepartmentRequest;
import com.auth.dto.SubCategoryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DepartmentConfigServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private DepartmentCatalog departmentCatalog;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void addDepartmentCreatesFileAndRefreshesCatalog() throws Exception {
        Path file = tempDir.resolve("departments.json");
        DepartmentConfigService service = newService(file);

        DepartmentRequest request = buildDepartmentRequest("dept-1", "CENTRAL");

        StepVerifier.create(service.addDepartment(request))
                .verifyComplete();

        ObjectNode root = mapper.readValue(file.toFile(), ObjectNode.class);
        ArrayNode centrals = root.withArray("centralGovernmentDepartments");
        assertThat(centrals).hasSize(1);
        assertThat(centrals.get(0).path("categories")).isNotNull();

        verify(departmentCatalog).refresh();
    }

    @Test
    void addDepartmentSupportsStateLevelAndNullCategories() throws Exception {
        Path file = tempDir.resolve("departments-state.json");
        DepartmentConfigService service = newService(file);

        DepartmentRequest request = new DepartmentRequest();
        request.setId("dept-9");
        request.setName("State Dept");
        request.setLevel("STATE");
        request.setCategories(null);

        StepVerifier.create(service.addDepartment(request)).verifyComplete();

        ObjectNode root = mapper.readValue(file.toFile(), ObjectNode.class);
        assertThat(root.withArray("stateGovernmentDepartments")).hasSize(1);
    }

    @Test
    void addDepartmentRejectsDuplicate() throws Exception {
        Path file = tempDir.resolve("departments.json");
        String existing = """
                {
                  "centralGovernmentDepartments": [ { "id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": [] } ],
                  "stateGovernmentDepartments": []
                }
                """;
        Files.writeString(file, existing);

        DepartmentConfigService service = newService(file);
        DepartmentRequest request = buildDepartmentRequest("dept-1", "CENTRAL");

        StepVerifier.create(service.addDepartment(request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT))
                .verify();

        verifyNoInteractions(departmentCatalog);
    }

    @Test
    void deleteDepartmentRemovesEntry() throws Exception {
        Path file = tempDir.resolve("departments.json");
        String existing = """
                {
                  "centralGovernmentDepartments": [ { "id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": [] } ],
                  "stateGovernmentDepartments": []
                }
                """;
        Files.writeString(file, existing);

        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteDepartment("dept-1"))
                .verifyComplete();

        ObjectNode root = mapper.readValue(file.toFile(), ObjectNode.class);
        assertThat(root.withArray("centralGovernmentDepartments")).isEmpty();
        verify(departmentCatalog).refresh();
    }

    @Test
    void deleteDepartmentRemovesStateEntry() throws Exception {
        Path file = tempDir.resolve("departments-state.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [],
                  "stateGovernmentDepartments": [ { "id": "DEPT-2", "name": "Existing", "level": "STATE", "categories": [] } ]
                }
                """);

        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteDepartment("dept-2"))
                .verifyComplete();
    }

    @Test
    void deleteDepartmentRequiresId() {
        DepartmentConfigService service = newService(tempDir.resolve("departments.json"));

        StepVerifier.create(service.deleteDepartment("  "))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();
    }

    @Test
    void deleteDepartmentWithNonMatchingIdsTriggersNotFoundBranch() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ { "id": "OTHER", "name": "Existing", "level": "CENTRAL", "categories": [] } ],
                  "stateGovernmentDepartments": []
                }
                """);
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteDepartment("dept-1"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();
    }

    @Test
    void deleteDepartmentNotFound() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {"centralGovernmentDepartments": [], "stateGovernmentDepartments": []}
                """);
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteDepartment("missing"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();
    }

    @Test
    void addCategoryAddsNewCategory() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ { "id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": [] } ],
                  "stateGovernmentDepartments": []
                }
                """);
        DepartmentConfigService service = newService(file);

        CategoryRequest request = new CategoryRequest();
        request.setCode("cat-1");
        request.setName("Category One");

        StepVerifier.create(service.addCategory("dept-1", request))
                .verifyComplete();

        ObjectNode root = mapper.readValue(file.toFile(), ObjectNode.class);
        ArrayNode categories = root.withArray("centralGovernmentDepartments").get(0).withArray("categories");
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).path("code").asText()).isEqualTo("CAT-1");
    }

    @Test
    void addCategoryRejectsDuplicate() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ {
                    "id": "DEPT-1",
                    "name": "Existing",
                    "level": "CENTRAL",
                    "categories": [ { "code": "CAT-1", "name": "Category One" } ]
                  } ],
                  "stateGovernmentDepartments": []
                }
                """);
        DepartmentConfigService service = newService(file);

        CategoryRequest request = new CategoryRequest();
        request.setCode("cat-1");
        request.setName("Category One");

        StepVerifier.create(service.addCategory("dept-1", request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT))
                .verify();
    }

    @Test
    void addCategoryValidatesDepartmentAndCode() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {"centralGovernmentDepartments": [], "stateGovernmentDepartments": []}
                """);
        DepartmentConfigService service = newService(file);

        CategoryRequest request = new CategoryRequest();
        request.setCode("cat-1");
        request.setName("Category One");

        StepVerifier.create(service.addCategory("missing", request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();

        request.setCode(null);
        Files.writeString(file, """
                {"centralGovernmentDepartments": [ {"id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": []} ], "stateGovernmentDepartments": []}
                """);

        StepVerifier.create(service.addCategory("dept-1", request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();

        Files.writeString(file, """
                {"centralGovernmentDepartments": [ {"id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": []} ], "stateGovernmentDepartments": []}
                """);

        request.setCode("  ");
        StepVerifier.create(service.addCategory("dept-1", request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();
    }

    @Test
    void deleteCategoryRemovesEntry() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ {
                    "id": "DEPT-1",
                    "name": "Existing",
                    "level": "CENTRAL",
                    "categories": [ { "code": "CAT-1", "name": "Category One" } ]
                  } ],
                  "stateGovernmentDepartments": []
                }
                """);
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteCategory("dept-1", "cat-1"))
                .verifyComplete();

        ObjectNode root = mapper.readValue(file.toFile(), ObjectNode.class);
        int categoriesCount = root.withArray("centralGovernmentDepartments")
                .get(0)
                .withArray("categories")
                .size();
        assertThat(categoriesCount).isZero();
    }

    @Test
    void deleteCategoryNotFoundOrDepartmentMissing() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {"centralGovernmentDepartments": [], "stateGovernmentDepartments": []}
                """);
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.deleteCategory("missing", "cat"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();

        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ { "id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": [] } ],
                  "stateGovernmentDepartments": []
                }
                """);

        StepVerifier.create(service.deleteCategory("dept-1", "cat-1"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();

        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ { "id": "DEPT-1", "name": "Existing", "level": "CENTRAL", "categories": [ { "code": "CAT-2", "name": "Another" } ] } ],
                  "stateGovernmentDepartments": []
                }
                """);

        StepVerifier.create(service.deleteCategory("dept-1", "cat-1"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();
    }

    @Test
    void getCategoriesReturnsArrayOrNotFound() throws Exception {
        Path file = tempDir.resolve("departments.json");
        Files.writeString(file, """
                {
                  "centralGovernmentDepartments": [ {
                    "id": "DEPT-1",
                    "name": "Existing",
                    "level": "CENTRAL",
                    "categories": [ { "code": "CAT-1", "name": "Category One" } ]
                  } ],
                  "stateGovernmentDepartments": []
                }
                """);
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.getCategories("dept-1"))
                .assertNext(array -> assertThat(array).hasSize(1))
                .verifyComplete();

        StepVerifier.create(service.getCategories("missing"))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND))
                .verify();
    }

    @Test
    void getAllDepartmentsInitializesMissingFile() {
        Path file = tempDir.resolve("departments.json");
        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.getAllDepartments())
                .assertNext(node -> {
                    assertThat(node.has("centralGovernmentDepartments")).isTrue();
                    assertThat(node.has("stateGovernmentDepartments")).isTrue();
                })
                .verifyComplete();

        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    void invalidDepartmentsFileStructureThrowsServerError() throws Exception {
        Path file = tempDir.resolve("bad-structure.json");
        Files.writeString(file, "[]");

        DepartmentConfigService service = newService(file);

        StepVerifier.create(service.getAllDepartments())
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.INTERNAL_SERVER_ERROR))
                .verify();
    }

    @Test
    void invalidLevelIsRejected() {
        Path file = tempDir.resolve("departments.json");
        DepartmentConfigService service = newService(file);

        DepartmentRequest request = new DepartmentRequest();
        request.setId("dept-3");
        request.setName("Dept");
        request.setLevel("municipal");

        StepVerifier.create(service.addDepartment(request))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();
    }

    @Test
    void missingLevelOrIdIsRejected() {
        DepartmentConfigService service = newService(tempDir.resolve("departments.json"));

        DepartmentRequest missingLevel = new DepartmentRequest();
        missingLevel.setId("dept-4");
        missingLevel.setName("Dept");

        StepVerifier.create(service.addDepartment(missingLevel))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();

        DepartmentRequest missingId = new DepartmentRequest();
        missingId.setLevel("CENTRAL");
        missingId.setName("Dept");

        StepVerifier.create(service.addDepartment(missingId))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();

        DepartmentRequest blankLevel = new DepartmentRequest();
        blankLevel.setId("dept-5");
        blankLevel.setName("Dept");
        blankLevel.setLevel("   ");

        StepVerifier.create(service.addDepartment(blankLevel))
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST))
                .verify();
    }

    @Test
    void serviceSupportsFilePrefixPaths() throws IOException {
        Path file = tempDir.resolve("prefixed.json");
        Files.deleteIfExists(file);

        DepartmentConfigService service = new DepartmentConfigService(
                "file:" + file.toString(),
                mapper,
                departmentCatalog);

        DepartmentRequest request = buildDepartmentRequest("dept-10", "CENTRAL");

        StepVerifier.create(service.addDepartment(request)).verifyComplete();
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    void classpathDepartmentsFileIsRejectedForWrites() {
        DepartmentConfigService service = new DepartmentConfigService(
                "classpath:departments-config/departments-db.json",
                mapper,
                departmentCatalog);

        StepVerifier.create(service.getAllDepartments())
                .expectErrorSatisfies(ex -> assertStatus(ex, HttpStatus.INTERNAL_SERVER_ERROR))
                .verify();
    }

    private DepartmentConfigService newService(Path file) {
        return new DepartmentConfigService(
                file.toString(),
                mapper,
                departmentCatalog);
    }

    private DepartmentRequest buildDepartmentRequest(String id, String level) {
        SubCategoryRequest subCategoryRequest = new SubCategoryRequest();
        subCategoryRequest.setCode("sub-1");
        subCategoryRequest.setName("Sub One");

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setCode("cat-1");
        categoryRequest.setName("Category One");
        categoryRequest.setSubCategories(List.of(subCategoryRequest));

        DepartmentRequest request = new DepartmentRequest();
        request.setId(id);
        request.setName("Department Name");
        request.setLevel(level);
        request.setCategories(List.of(categoryRequest));
        return request;
    }

    private void assertStatus(Throwable throwable, HttpStatus status) {
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) throwable).getStatusCode()).isEqualTo(status);
    }
}
