package com.notifiction.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.notifiction.event.GrievanceEvent;
import com.notifiction.model.NotificationLogs;
import com.notifiction.model.NotificationStatus;
import com.notifiction.model.Notifications;
import com.notifiction.repository.NotificationLogRepository;
import com.notifiction.repository.NotificationRepository;
import com.notifiction.service.NotificationSender;

import reactor.core.publisher.Mono;

class GrievanceEventConsumerTest {

    @Test
    void consumePersistsNotificationAndLogs() {
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        NotificationLogRepository logRepository = mock(NotificationLogRepository.class);
        NotificationSender notificationSender = mock(NotificationSender.class);

        AtomicBoolean logSaved = new AtomicBoolean(false);
        when(notificationRepository.save(any(Notifications.class))).thenAnswer(invocation -> {
            Notifications saved = invocation.getArgument(0);
            saved.setId("notif-id");
            return Mono.just(saved);
        });
        when(logRepository.save(any(NotificationLogs.class))).thenAnswer(invocation -> {
            logSaved.set(true);
            return Mono.just(invocation.getArgument(0));
        });
        when(notificationSender.sendSms(any())).thenReturn(Mono.empty());
        when(notificationSender.sendEmail(any())).thenReturn(Mono.empty());

        GrievanceEventConsumer consumer = new GrievanceEventConsumer();
        ReflectionTestUtils.setField(consumer, "notificationRepository", notificationRepository);
        ReflectionTestUtils.setField(consumer, "logRepository", logRepository);
        ReflectionTestUtils.setField(consumer, "notificationSender", notificationSender);

        GrievanceEvent event = new GrievanceEvent();
        event.setUserId("user-1");
        event.setEventType("SUBMITTED");
        event.setMessage("message body");

        consumer.consume(event);

        ArgumentCaptor<Notifications> notificationCaptor = ArgumentCaptor.forClass(Notifications.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notificationCaptor.getValue().getMessage()).isEqualTo("message body");

        verify(logRepository).save(any(NotificationLogs.class));
        verify(notificationSender).sendSms(event);
        verify(notificationSender).sendEmail(event);
        assertThat(logSaved).isTrue();
    }
}
