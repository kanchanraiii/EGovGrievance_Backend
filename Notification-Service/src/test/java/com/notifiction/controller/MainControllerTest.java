package com.notifiction.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import com.notifiction.model.Notifications;
import com.notifiction.repository.NotificationRepository;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class MainControllerTest {

    @Test
    void getByUserDelegatesToRepository() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notifications notification = new Notifications();
        notification.setUserId("user-1");
        when(repository.findByUserId("user-1")).thenReturn(Flux.just(notification));

        MainController controller = new MainController(repository);

        StepVerifier.create(controller.getByUser("user-1"))
                .assertNext(result -> assertThat(result.getUserId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository).findByUserId("user-1");
    }
}
