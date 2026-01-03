package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

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
}
