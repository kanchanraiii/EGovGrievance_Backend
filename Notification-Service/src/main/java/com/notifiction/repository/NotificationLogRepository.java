package com.notifiction.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.notifiction.model.NotificationLogs;

public interface NotificationLogRepository extends ReactiveMongoRepository<NotificationLogs, String> {
}
