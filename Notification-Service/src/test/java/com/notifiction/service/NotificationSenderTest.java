package com.notifiction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import com.notifiction.event.GrievanceEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class NotificationSenderTest {

    private GrievanceEvent sampleEvent() {
        GrievanceEvent event = new GrievanceEvent();
        event.setUserId("user@example.com");
        event.setEventType("SUBMITTED");
        event.setMessage("test message");
        return event;
    }

    @Test
    void sendSmsIsNoopWhenDisabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationSender sender = new NotificationSender(
                mailSender,
                "disabled",
                "",
                "",
                "",
                "",
                "default@example.com"
        );

        StepVerifier.create(sender.sendSms(sampleEvent()))
                .verifyComplete();
    }

    @Test
    void sendSmsInvokesWebClientWhenEnabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationSender sender = new NotificationSender(
                mailSender,
                "AC123",
                "auth",
                "+100000",
                "+200000",
                "no-reply@example.com",
                "default@example.com"
        );

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK).body("ok").build());
        };
        WebClient stubClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        ReflectionTestUtils.setField(sender, "webClient", stubClient);

        StepVerifier.create(sender.sendSms(sampleEvent()))
                .verifyComplete();

        assertThat(capturedRequest.get()).isNotNull();
        assertThat(capturedRequest.get().url().getPath()).contains("/AC123/");
    }

    @Test
    void sendEmailIsNoopWhenDisabled() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationSender sender = new NotificationSender(
                mailSender,
                "disabled",
                "",
                "",
                "",
                "",
                "default@example.com"
        );

        StepVerifier.create(sender.sendEmail(sampleEvent()))
                .verifyComplete();

        verify(mailSender, times(0)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailUsesUserAddressWhenPresent() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        NotificationSender sender = new NotificationSender(
                mailSender,
                "disabled",
                "",
                "",
                "",
                "no-reply@example.com",
                "default@example.com"
        );

        GrievanceEvent event = sampleEvent();

        StepVerifier.create(sender.sendEmail(event))
                .verifyComplete();

        verify(mailSender, times(1)).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("no-reply@example.com");
        assertThat(mimeMessage.getSubject()).contains("SUBMITTED");
    }
}
