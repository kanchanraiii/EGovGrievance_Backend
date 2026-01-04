package com.storage;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class StorageServiceApplicationMainTest {

    @Test
    void mainRunsWithCustomProperties() {
        assertThatCode(() -> StorageServiceApplication.main(new String[]{
                "--spring.main.web-application-type=none",
                "--spring.cloud.discovery.enabled=false",
                "--spring.data.mongodb.uri=mongodb://localhost:27017/test"
        })).doesNotThrowAnyException();
    }
}
