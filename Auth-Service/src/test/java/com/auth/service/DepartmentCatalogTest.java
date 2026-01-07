package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

class DepartmentCatalogTest {

    @Test
    void returnsFalseWhenFileMissing() {
        DepartmentCatalog catalog = new DepartmentCatalog(
                "non-existent-departments.json",
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("ABC")).isFalse();
    }

    @Test
    void loadsIdsFromJsonFile() throws IOException {
        String json = """
                {
                  "centralGovernmentDepartments": [ { "id": "CENT-1" } ],
                  "stateGovernmentDepartments": [ { "id": "state-2" } ]
                }
                """;
        Path temp = Files.createTempFile("departments-db", ".json");
        Files.writeString(temp, json);

        DepartmentCatalog catalog = new DepartmentCatalog(
                temp.toString(),
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("cent-1")).isTrue();
        assertThat(catalog.isValid("STATE-2")).isTrue();
        assertThat(catalog.isValid("missing")).isFalse();
    }

    @Test
    void refreshReloadsAndHandlesInvalidContent() throws Exception {
        Path temp = Files.createTempFile("departments-db-invalid", ".json");
        Files.writeString(temp, "not-json");

        DepartmentCatalog catalog = new DepartmentCatalog(
                temp.toString(),
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("ANY")).isFalse(); // hasDepartmentData false

        String validJson = """
                {
                  "centralGovernmentDepartments": [ { "id": "CENT-1" } ],
                  "stateGovernmentDepartments": []
                }
                """;
        Files.writeString(temp, validJson);

        catalog.refresh();

        assertThat(catalog.isValid("CENT-1")).isTrue();
    }

    @Test
    void loadDepartmentIdsFallsBackWhenNoResourcesPresent() throws Exception {
        Path temp = Files.createTempDirectory("dept-loader");

        DepartmentCatalog catalog = new DepartmentCatalog(
                "file:" + temp.resolve("missing.json"),
                new ResourceLoader() {
                    @Override
                    public Resource getResource(String location) {
                        return new org.springframework.core.io.AbstractResource() {
                            @Override
                            public String getDescription() {
                                return location;
                            }

                            @Override
                            public java.io.InputStream getInputStream() throws java.io.IOException {
                                throw new java.io.FileNotFoundException(location);
                            }

                            @Override
                            public boolean exists() {
                                return false;
                            }
                        };
                    }

                    @Override
                    public ClassLoader getClassLoader() {
                        return getClass().getClassLoader();
                    }
                },
                new ObjectMapper());

        assertThat(catalog.isValid("ANY")).isFalse();
    }

    @Test
    void supportsClasspathPathPrefix() {
        DepartmentCatalog catalog = new DepartmentCatalog(
                "classpath:departments-config/departments-db.json",
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("NON_EXISTENT")).isFalse();
    }

    @Test
    void returnsFalseForNullDepartmentId() {
        DepartmentCatalog catalog = new DepartmentCatalog(
                "classpath:departments-config/departments-db.json",
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid(null)).isFalse();
    }

    @Test
    void returnsFalseForEmptyDepartmentId() {
        DepartmentCatalog catalog = new DepartmentCatalog(
                "classpath:departments-config/departments-db.json",
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("   ")).isFalse();
    }

    @Test
    void handlesMissingArraysGracefully() throws Exception {
        Path temp = Files.createTempFile("departments-db-empty-arrays", ".json");
        Files.writeString(temp, """
                { "centralGovernmentDepartments": null, "stateGovernmentDepartments": null }
                """);

        DepartmentCatalog catalog = new DepartmentCatalog(
                temp.toString(),
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("ANY")).isFalse();
    }

    @Test
    void handlesNonArrayDepartmentsAndNonTextIds() throws Exception {
        Path temp = Files.createTempFile("departments-db-non-array", ".json");
        Files.writeString(temp, """
                { "centralGovernmentDepartments": {}, "stateGovernmentDepartments": [ { "id": 123 } ] }
                """);

        DepartmentCatalog catalog = new DepartmentCatalog(
                temp.toString(),
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("123")).isFalse();
    }

    @Test
    void refreshUpdatesHasDepartmentDataWhenFileHasEntries() throws Exception {
        Path temp = Files.createTempFile("departments-db-refresh", ".json");
        Files.writeString(temp, """
                { "centralGovernmentDepartments": [ { "id": "DEPT-X" } ], "stateGovernmentDepartments": [] }
                """);

        DepartmentCatalog catalog = new DepartmentCatalog(
                temp.toString(),
                new DefaultResourceLoader(),
                new ObjectMapper());

        assertThat(catalog.isValid("DEPT-X")).isTrue();

        Files.writeString(temp, """
                { "centralGovernmentDepartments": [], "stateGovernmentDepartments": [] }
                """);

        catalog.refresh();

        assertThat(catalog.isValid("DEPT-X")).isFalse();
    }
}
