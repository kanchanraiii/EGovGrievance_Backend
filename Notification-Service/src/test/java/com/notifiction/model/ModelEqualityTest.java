package com.notifiction.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5838")
class ModelEqualityTest {

    @Test
    void notificationsEqualityAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        Notifications n1 = new Notifications();
        n1.setId("n1");
        n1.setUserId("user");
        n1.setMessage("msg");
        n1.setType(NotificationType.EMAIL);
        n1.setStatus(NotificationStatus.SENT);
        n1.setCreatedAt(now);

        Notifications n2 = new Notifications();
        n2.setId("n1");
        n2.setUserId("user");
        n2.setMessage("msg");
        n2.setType(NotificationType.EMAIL);
        n2.setStatus(NotificationStatus.SENT);
        n2.setCreatedAt(now);

        assertThat(n1).isEqualTo(n2).hasSameHashCodeAs(n2);
        assertThat(n1.toString()).contains("msg");

        n2.setMessage("other");
        assertThat(n1).isNotEqualTo(n2)
                .isEqualTo(n1)
                .isNotEqualTo(null)
                .isNotEqualTo("other");

        Notifications empty1 = new Notifications();
        Notifications empty2 = new Notifications();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        Notifications nullUser = new Notifications();
        nullUser.setId("n1");
        Notifications withUser = new Notifications();
        withUser.setId("n1");
        withUser.setUserId("user");
        assertThat(nullUser).isNotEqualTo(withUser);

        Notifications nullStatus = new Notifications();
        nullStatus.setId("n1");
        nullStatus.setUserId("user");
        Notifications withStatus = new Notifications();
        withStatus.setId("n1");
        withStatus.setUserId("user");
        withStatus.setStatus(NotificationStatus.SENT);
        assertThat(nullStatus).isNotEqualTo(withStatus);

        Notifications refusing = new Notifications() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusing.setId("n1");
        assertThat(n1.equals(refusing)).isFalse();
    }

    @Test
    void notificationLogsEqualityAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        NotificationLogs l1 = new NotificationLogs();
        l1.setId("l1");
        l1.setNotificationId("n1");
        l1.setResponse("ok");
        l1.setLoggedAt(now);

        NotificationLogs l2 = new NotificationLogs();
        l2.setId("l1");
        l2.setNotificationId("n1");
        l2.setResponse("ok");
        l2.setLoggedAt(now);

        assertThat(l1).isEqualTo(l2).hasSameHashCodeAs(l2);
        assertThat(l1.toString()).contains("ok");
        assertThat(l1).isEqualTo(l1);

        l2.setNotificationId("other");
        assertThat(l1).isNotEqualTo(l2)
                .isNotEqualTo(null)
                .isNotEqualTo("other");

        NotificationLogs empty1 = new NotificationLogs();
        NotificationLogs empty2 = new NotificationLogs();
        assertThat(empty1).isEqualTo(empty2);
        assertThat(empty1.hashCode()).isNotZero();

        NotificationLogs nullResponse = new NotificationLogs();
        nullResponse.setId("l1");
        NotificationLogs withResponse = new NotificationLogs();
        withResponse.setId("l1");
        withResponse.setResponse("ok");
        assertThat(nullResponse).isNotEqualTo(withResponse);

        NotificationLogs nullLoggedAt = new NotificationLogs();
        nullLoggedAt.setId("l1");
        nullLoggedAt.setNotificationId("n1");
        NotificationLogs withLoggedAt = new NotificationLogs();
        withLoggedAt.setId("l1");
        withLoggedAt.setNotificationId("n1");
        withLoggedAt.setLoggedAt(now);
        assertThat(nullLoggedAt).isNotEqualTo(withLoggedAt);

        NotificationLogs refusing = new NotificationLogs() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        refusing.setId("l1");
        assertThat(l1.equals(refusing)).isFalse();
    }

    @Test
    void notificationsCoverNullAndDifferentBranches() {
        LocalDateTime now = LocalDateTime.now();
        Notifications base = populatedNotification(now);

        Notifications noId = populatedNotification(now);
        noId.setId(null);
        Notifications differentId = populatedNotification(now);
        differentId.setId("other");

        Notifications missingBaseId = new Notifications();
        missingBaseId.setUserId("user");
        Notifications otherWithId = new Notifications();
        otherWithId.setId("n1");
        assertThat(missingBaseId).isNotEqualTo(otherWithId);

        Notifications differentUser = populatedNotification(now);
        differentUser.setUserId("another");
        Notifications messageNull = populatedNotification(now);
        messageNull.setMessage(null);

        Notifications messageMissingOnBase = populatedNotification(now);
        messageMissingOnBase.setMessage(null);
        Notifications withMessage = populatedNotification(now);
        assertThat(messageMissingOnBase).isNotEqualTo(withMessage);

        Notifications typeChanged = populatedNotification(now);
        typeChanged.setType(NotificationType.IN_APP);
        Notifications typeCleared = populatedNotification(now);
        typeCleared.setType(null);
        Notifications typeMissingOnBase = populatedNotification(now);
        typeMissingOnBase.setType(null);
        Notifications withType = populatedNotification(now);
        assertThat(typeMissingOnBase).isNotEqualTo(withType);

        Notifications statusChanged = populatedNotification(now);
        statusChanged.setStatus(NotificationStatus.FAILED);
        Notifications nullCreatedAt = populatedNotification(now);
        nullCreatedAt.setCreatedAt(null);
        Notifications createdAtMissingOnBase = populatedNotification(null);
        Notifications withCreatedAt = populatedNotification(now);
        assertThat(createdAtMissingOnBase).isNotEqualTo(withCreatedAt);

        Notifications createdAtDifferent = populatedNotification(now.minusDays(1));
        assertThat(base).isNotEqualTo(noId)
                .isNotEqualTo(differentId)
                .isNotEqualTo(differentUser)
                .isNotEqualTo(messageNull)
                .isNotEqualTo(typeChanged)
                .isNotEqualTo(typeCleared)
                .isNotEqualTo(statusChanged)
                .isNotEqualTo(nullCreatedAt)
                .isNotEqualTo(createdAtDifferent);
    }

    @Test
    void notificationLogsCoverNullAndDifferentBranches() {
        LocalDateTime now = LocalDateTime.now();
        NotificationLogs base = populatedLog(now);

        NotificationLogs noId = populatedLog(now);
        noId.setId(null);
        NotificationLogs baseWithoutId = new NotificationLogs();
        baseWithoutId.setNotificationId("n1");
        NotificationLogs withOnlyId = new NotificationLogs();
        withOnlyId.setId("l1");
        assertThat(baseWithoutId).isNotEqualTo(withOnlyId);

        NotificationLogs differentId = populatedLog(now);
        differentId.setId("other");
        NotificationLogs differentNotificationId = populatedLog(now);
        differentNotificationId.setNotificationId("other");
        NotificationLogs nullResponse = populatedLog(now);
        nullResponse.setResponse(null);
        NotificationLogs responseMissingOnBase = populatedLog(now);
        responseMissingOnBase.setResponse(null);
        NotificationLogs withResponse = populatedLog(now);
        assertThat(responseMissingOnBase).isNotEqualTo(withResponse);

        NotificationLogs nullNotificationOnBase = populatedLog(now);
        nullNotificationOnBase.setNotificationId(null);
        NotificationLogs withNotification = populatedLog(now);
        assertThat(nullNotificationOnBase).isNotEqualTo(withNotification);

        NotificationLogs nullLoggedAt = populatedLog(now);
        nullLoggedAt.setLoggedAt(null);
        NotificationLogs loggedAtMissingOnBase = populatedLog(null);
        NotificationLogs withLoggedAt = populatedLog(now);
        assertThat(loggedAtMissingOnBase).isNotEqualTo(withLoggedAt);

        NotificationLogs loggedAtChanged = populatedLog(now.minusHours(1));
        assertThat(base).isNotEqualTo(noId)
                .isNotEqualTo(differentId)
                .isNotEqualTo(differentNotificationId)
                .isNotEqualTo(nullResponse)
                .isNotEqualTo(nullLoggedAt)
                .isNotEqualTo(loggedAtChanged);
    }

    @Test
    void enumsExposeExpectedValues() {
        assertThat(NotificationType.valueOf("EMAIL")).isEqualTo(NotificationType.EMAIL);
        assertThat(NotificationStatus.valueOf("SENT")).isEqualTo(NotificationStatus.SENT);
        assertThat(NotificationType.values()).contains(NotificationType.IN_APP);
        assertThat(NotificationStatus.values()).contains(NotificationStatus.FAILED);
    }

    private Notifications populatedNotification(LocalDateTime createdAt) {
        Notifications n1 = new Notifications();
        n1.setId("n1");
        n1.setUserId("user");
        n1.setMessage("msg");
        n1.setType(NotificationType.EMAIL);
        n1.setStatus(NotificationStatus.SENT);
        n1.setCreatedAt(createdAt);
        return n1;
    }

    private NotificationLogs populatedLog(LocalDateTime loggedAt) {
        NotificationLogs log = new NotificationLogs();
        log.setId("l1");
        log.setNotificationId("n1");
        log.setResponse("ok");
        log.setLoggedAt(loggedAt);
        return log;
    }
}
