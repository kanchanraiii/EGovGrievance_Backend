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
    private final boolean smsEnabled;
    private final boolean emailEnabled;

    @Autowired
    public NotificationSender(
            JavaMailSender mailSender,
            @Value("${twilio.account-sid:disabled}") String accountSid,
            @Value("${twilio.auth-token:}") String authToken,
            @Value("${twilio.from:}") String twilioFrom,
            @Value("${twilio.to:}") String twilioTo,
            @Value("${mail.from:}") String emailFrom
    ) {
        this.mailSender = mailSender;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.twilioFrom = twilioFrom;
        this.twilioTo = twilioTo;
        this.emailFrom = emailFrom;
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
                String recipient = StringUtils.hasText(event.getEmail()) ? event.getEmail() : event.getUserId();
                if (!StringUtils.hasText(recipient) || !recipient.contains("@")) {
                    log.warn("Skipping email send: no valid recipient for grievance {}", event.getGrievanceId());
                    return;
                }
                helper.setTo(recipient);
                helper.setSubject("Grievance Update: " + event.getEventType());
                helper.setText(buildHtmlBody(event), true);
                mailSender.send(mimeMessage);
            } catch (Exception e) {
                log.warn("Email send failed: {}", e.getMessage());
            }
        }).then();
    }

    private String buildHtmlBody(GrievanceEvent event) {
        String safeMessage = StringUtils.hasText(event.getMessage()) ? event.getMessage() : "We will keep you posted.";
        String grievanceId = StringUtils.hasText(event.getGrievanceId()) ? event.getGrievanceId() : "your grievance";
        String status = StringUtils.hasText(event.getEventType()) ? event.getEventType() : "UPDATE";

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; background:#f7f9fb; margin:0; padding:0;">
                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                      <tr>
                        <td align="center" style="padding:24px;">
                          <table role="presentation" cellpadding="0" cellspacing="0" width="600" style="background:#ffffff;border-radius:12px;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                            <tr>
                              <td style="padding:24px 28px 12px 28px;">
                                <div style="font-size:12px;letter-spacing:1px;color:#5b6b7a;text-transform:uppercase;">Grievance Update</div>
                                <h2 style="margin:8px 0 0 0;color:#0f172a;">%s</h2>
                                <p style="margin:12px 0 0 0;color:#64748b;font-size:14px;">Status: <strong style="color:#2563eb;">%s</strong></p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:0 28px 24px 28px;">
                                <div style="background:#f1f5f9;border-radius:10px;padding:16px;color:#0f172a;font-size:14px;line-height:1.6;">
                                  %s
                                </div>
                                <p style="margin:16px 0 0 0;color:#94a3b8;font-size:12px;">You are receiving this update because you lodged this grievance.</p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(grievanceId, status, safeMessage);
    }
}
