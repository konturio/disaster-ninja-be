package io.kontur.disasterninja.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;

@Configuration
@ConditionalOnProperty(value = "notifications.enabled")
public class MailSenderConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(MailSenderConfiguration.class);

    @Value("${notifications.smtpUsername}")
    private String smtpUsername;

    @Value("${notifications.smtpPassword}")
    private String smtpPassword;

    @Value("${notifications.smtpHost}")
    private String smtpHost;

    @Value("${notifications.smtpPort}")
    private String smtpPort;

    @Bean
    public JavaMailSender javaMailSender() {
        if (isAnyBlank(smtpUsername, smtpPassword, smtpPort, smtpHost)) {
            LOG.error("SMTP configuration is not complete!");
            return null;
        }
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(Integer.parseInt(smtpPort));
        mailSender.setUsername(smtpUsername);
        mailSender.setPassword(smtpPassword);

        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
