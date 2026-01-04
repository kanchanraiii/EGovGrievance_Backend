package com.notifiction.consumer;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.notifiction.event.GrievanceEvent;
import com.notifiction.model.*;
import com.notifiction.repository.*;
import com.notifiction.service.NotificationSender;

import reactor.core.publisher.Mono;

@Component
public class GrievanceEventConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationSender notificationSender;

    public GrievanceEventConsumer(NotificationRepository notificationRepository,
                                  NotificationLogRepository logRepository,
                                  NotificationSender notificationSender) {
        this.notificationRepository = notificationRepository;
        this.logRepository = logRepository;
        this.notificationSender = notificationSender;
    }

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
                return logRepository.save(log).thenReturn(saved);
            })
            .flatMap(saved -> notificationSender.sendSms(event)
                    .then(notificationSender.sendEmail(event))
                    .onErrorResume(ex -> Mono.empty())
                    .thenReturn(saved))
            .onErrorResume(ex -> Mono.empty())
            .subscribe();
    }
}
