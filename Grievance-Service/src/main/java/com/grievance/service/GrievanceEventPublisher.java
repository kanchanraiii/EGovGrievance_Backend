package com.grievance.service;

import com.grievance.event.GrievanceEvent;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class GrievanceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GrievanceEventPublisher.class);
    private static final String TOPIC = "grievance-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GrievanceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> publishStatusChange(Grievance grievance, GrievanceStatus newStatus, String remarks) {
        GrievanceEvent event = buildEvent(grievance, newStatus, remarks);

        return Mono.fromCallable(() -> kafkaTemplate.send(TOPIC, event))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.warn("Failed to publish grievance {} event {}", grievance.getId(), newStatus, error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    private GrievanceEvent buildEvent(Grievance grievance, GrievanceStatus newStatus, String remarks) {
        GrievanceEvent event = new GrievanceEvent();
        event.setGrievanceId(grievance.getId());
        event.setUserId(grievance.getCitizenId());
        event.setEventType(newStatus.name());
        event.setMessage(buildMessage(grievance.getId(), newStatus, remarks));
        return event;
    }

    private String buildMessage(String grievanceId, GrievanceStatus status, String remarks) {
        String friendlyStatus = status.name().replace("_", " ").toLowerCase();
        String baseMessage = "Your grievance " + grievanceId + " is now " + friendlyStatus + ".";

        if (remarks != null && !remarks.isBlank()) {
            return baseMessage + " " + remarks.trim();
        }

        return baseMessage + " We will keep you posted.";
    }
}
