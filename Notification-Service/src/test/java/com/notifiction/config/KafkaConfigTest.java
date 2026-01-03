package com.notifiction.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import com.notifiction.event.GrievanceEvent;

class KafkaConfigTest {

    @Test
    void createsTopicAndConsumerFactoryWithProvidedProperties() {
        KafkaConfig config = new KafkaConfig();
        // inject custom bootstrap and group id
        org.springframework.test.util.ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:1234");
        org.springframework.test.util.ReflectionTestUtils.setField(config, "consumerGroupId", "group-1");

        NewTopic topic = config.grievanceEventsTopic();
        assertThat(topic.name()).isEqualTo("grievance-events");
        assertThat(topic.numPartitions()).isEqualTo(1);

        ConsumerFactory<String, GrievanceEvent> factory = config.consumerFactory();
        Map<String, Object> props = factory.getConfigurationProperties();
        assertThat(props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:1234");
        assertThat(props.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("group-1");

        var listenerFactory = config.kafkaListenerContainerFactory(factory);
        ConcurrentMessageListenerContainer<String, GrievanceEvent> container =
                listenerFactory.createContainer("grievance-events");
        assertThat(container).isNotNull();
    }
}
