package com.notifiction.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class MailConfigTest {

    @Test
    void javaMailSenderUsesProvidedSettings() {
        MailConfig config = new MailConfig();

        JavaMailSender sender = config.javaMailSender("smtp.example.com", 2525, "user", "pass");
        assertThat(sender).isInstanceOf(JavaMailSenderImpl.class);
        JavaMailSenderImpl impl = (JavaMailSenderImpl) sender;

        assertThat(impl.getHost()).isEqualTo("smtp.example.com");
        assertThat(impl.getPort()).isEqualTo(2525);
        assertThat(impl.getUsername()).isEqualTo("user");
        assertThat(impl.getPassword()).isEqualTo("pass");
        assertThat(impl.getJavaMailProperties()).containsEntry("mail.smtp.auth", "true");
        assertThat(impl.getJavaMailProperties()).containsEntry("mail.smtp.starttls.enable", "true");
    }
}
