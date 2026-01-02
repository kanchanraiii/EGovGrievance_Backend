package com.notifiction.service;

import com.notifiction.event.GrievanceEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final WebClient webClient;
    private final JavaMailSender mailSender;
    private final String accountSid;
    private final String authToken;
    private final String twilioFrom;
    private final String twilioTo;
    private final String emailFrom;
    private final String defaultEmailTo;
    private final boolean smsEnabled;
    private final boolean emailEnabled;

    @Autowired
    public NotificationSender(
            JavaMailSender mailSender,
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from}") String twilioFrom,
            @Value("${twilio.to}") String twilioTo,
            @Value("${mail.from}") String emailFrom,
            @Value("${mail.to}") String emailTo
    ) {
        this.mailSender = mailSender;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.twilioFrom = twilioFrom;
        this.twilioTo = twilioTo;
        this.emailFrom = emailFrom;
        this.defaultEmailTo = emailTo;
        this.smsEnabled = StringUtils.hasText(accountSid) && StringUtils.hasText(authToken)
                && StringUtils.hasText(twilioFrom) && StringUtils.hasText(twilioTo)
                && !accountSid.equalsIgnoreCase("disabled");
        this.emailEnabled = StringUtils.hasText(emailFrom);
        this.webClient = WebClient.builder()
                .baseUrl("https://api.twilio.com")
                .build();
    }

    public Mono<Void> sendSms(GrievanceEvent event) {
        if (!smsEnabled) {
            log.debug("SMS disabled; skipping send for event {}", event.getEventType());
            return Mono.empty();
        }

        String path = "/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        return webClient.post()
                .uri(path)
                .header("Authorization", authHeader)
                .body(BodyInserters.fromFormData("To", twilioTo)
                        .with("From", twilioFrom)
                        .with("Body", event.getMessage()))
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .onErrorResume(ex -> {
                    log.warn("SMS send failed: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Void> sendEmail(GrievanceEvent event) {
        if (!emailEnabled) {
            log.debug("Email disabled; skipping send for event {}", event.getEventType());
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(emailFrom);
                String recipient = (event.getUserId() != null && event.getUserId().contains("@"))
                        ? event.getUserId()
                        : defaultEmailTo;
                helper.setTo(recipient);
                helper.setSubject("Grievance Update: " + event.getEventType());
                helper.setText(event.getMessage(), false);
                mailSender.send(mimeMessage);
            } catch (Exception e) {
                log.warn("Email send failed: {}", e.getMessage());
            }
        }).then();
    }
}
