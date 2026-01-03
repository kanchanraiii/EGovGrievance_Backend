package com.grievance;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class GrievanceApplicationMainTest {

    @Test
    void mainStartsWithoutErrors() {
        String[] args = {"--spring.main.web-application-type=none", "--eureka.client.enabled=false"};
        assertThatCode(() -> {
            try (ConfigurableApplicationContext context = SpringApplication.run(GrievanceApplication.class, args)) {
                // context closes automatically
            }
        }).doesNotThrowAnyException();
    }
}
