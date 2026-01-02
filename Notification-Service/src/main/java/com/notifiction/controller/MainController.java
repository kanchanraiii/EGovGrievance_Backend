package com.notifiction.controller;

import com.notifiction.model.Notifications;
import com.notifiction.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/notifications")
public class MainController {

    @Autowired
	private NotificationRepository notificationRepository;

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public Flux<Notifications> getByUser(@PathVariable String userId) {
        return notificationRepository.findByUserId(userId);
    }
}
