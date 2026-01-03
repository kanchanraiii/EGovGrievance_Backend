package com.grievance.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaProducerConfigTest {

    @Test
    void producerFactoryUsesConfiguredBootstrap() {
        KafkaProducerConfig config = new KafkaProducerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:1234");

        ProducerFactory<String, Object> factory = config.producerFactory();
        Map<String, Object> props = ((DefaultKafkaProducerFactory<String, Object>) factory).getConfigurationProperties();

        assertThat(props.get("bootstrap.servers")).isEqualTo("localhost:1234");
        assertThat(props.get("key.serializer")).isNotNull();
        assertThat(props.get("value.serializer")).isNotNull();
    }

    @Test
    void kafkaTemplateBuildsFromProducerFactory() {
        KafkaProducerConfig config = new KafkaProducerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:1234");

        KafkaTemplate<String, Object> template = config.kafkaTemplate();
        assertThat(template).isNotNull();
    }
}
