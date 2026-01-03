package com.grievance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.grievance.event.GrievanceEvent;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GrievanceEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private GrievanceEventPublisher publisher;

    @Test
    void publishStatusChangeSendsEvent() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("user1");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(any(String.class), any())).thenReturn(future);

        StepVerifier.create(publisher.publishStatusChange(grievance, GrievanceStatus.SUBMITTED, "ok"))
                .verifyComplete();

        verify(kafkaTemplate, times(1)).send(any(String.class), any());
    }

    @Test
    void publishStatusChangeSwallowsSendErrors() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("user1");

        when(kafkaTemplate.send(any(String.class), any())).thenThrow(new RuntimeException("send failed"));

        StepVerifier.create(publisher.publishStatusChange(grievance, GrievanceStatus.RESOLVED, "done"))
                .verifyComplete();
    }

    @Test
    void publishStatusChangeUsesDefaultMessageWhenRemarksBlank() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("user1");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        ArgumentCaptor<GrievanceEvent> eventCaptor = ArgumentCaptor.forClass(GrievanceEvent.class);

        when(kafkaTemplate.send(any(String.class), eventCaptor.capture())).thenReturn(future);

        StepVerifier.create(publisher.publishStatusChange(grievance, GrievanceStatus.CLOSED, "   "))
                .verifyComplete();

        GrievanceEvent sent = eventCaptor.getValue();
        assertThat(sent.getMessage()).contains("grievance g1 is now closed").contains("keep you posted");
    }

    @Test
    void publishStatusChangeHandlesNullRemarksBranch() {
        Grievance grievance = new Grievance();
        grievance.setId("g1");
        grievance.setCitizenId("user1");

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        ArgumentCaptor<GrievanceEvent> eventCaptor = ArgumentCaptor.forClass(GrievanceEvent.class);

        when(kafkaTemplate.send(any(String.class), eventCaptor.capture())).thenReturn(future);

        StepVerifier.create(publisher.publishStatusChange(grievance, GrievanceStatus.WORK_DONE, null))
                .verifyComplete();

        assertThat(eventCaptor.getValue().getMessage()).contains("keep you posted");
    }
}
