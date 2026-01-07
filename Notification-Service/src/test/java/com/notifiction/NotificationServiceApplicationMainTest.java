package com.notifiction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.web-application-type=none",
                "spring.main.lazy-initialization=true",
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.kafka.bootstrap-servers=localhost:0",
                "spring.kafka.listener.auto-startup=false",
                "spring.data.mongodb.repositories.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaReactiveStreamsAutoConfiguration,org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,org.springframework.cloud.config.client.ConfigClientAutoConfiguration"
        }
)
class NotificationServiceApplicationMainTest {

    @Test
    void contextLoads() {
        // context initialization is the test
    }
}
