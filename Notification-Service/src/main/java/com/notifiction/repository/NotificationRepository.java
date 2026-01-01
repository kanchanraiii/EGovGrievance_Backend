package com.notifiction.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.notifiction.model.Notifications;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveMongoRepository<Notifications, String> {

	Flux<Notifications> findByUserId(String userId);
}
