package com.notifiction.consumer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.notifiction.event.GrievanceEvent;
import com.notifiction.model.*;
import com.notifiction.repository.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GrievanceEventConsumer {

    @Autowired
	private NotificationRepository notificationRepository;
    @Autowired
    private NotificationLogRepository logRepository;

    @KafkaListener(
        topics = "grievance-events",
        groupId = "notification-group"
    )
    public void consume(GrievanceEvent event) {

        Notifications notification = new Notifications();
        notification.setUserId(event.getUserId());
        notification.setMessage(event.getMessage());
        notification.setType(NotificationType.IN_APP); // default
        notification.setStatus(NotificationStatus.SENT);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification)
            .flatMap(saved -> {
                NotificationLogs log = new NotificationLogs();
                log.setNotificationId(saved.getId());
                log.setResponse("Delivered via IN_APP");
                log.setLoggedAt(LocalDateTime.now());
                return logRepository.save(log);
            })
            .onErrorResume(ex -> Mono.empty())
            .subscribe();
    }
}
