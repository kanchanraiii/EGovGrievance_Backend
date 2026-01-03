package com.notifiction;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class NotificationServiceApplicationMainTest {

    @Test
    void mainStartsWithExcludedAutoConfigs() {
        assertThatCode(() -> NotificationServiceApplication.main(new String[]{
                "--spring.main.web-application-type=none",
                "--spring.cloud.discovery.enabled=false",
                "--eureka.client.enabled=false",
                "--spring.kafka.bootstrap-servers=localhost:0",
                "--spring.data.mongodb.uri=mongodb://localhost:27017/test",
                "--jwt.secret=test-secret"
        })).doesNotThrowAnyException();
    }
}
