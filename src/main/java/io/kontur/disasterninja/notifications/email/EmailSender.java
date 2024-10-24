package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class EmailSender {

    private final static Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender javaMailSender;

    @Value("${notifications.sender}")
    private String sender;

    @Value("${notifications.recipients}")
    private String[] recipients;

    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void send(EmailDto emailDto) {
        if (javaMailSender == null) {
            throw new RuntimeException("Not able to proceed with email notifications because javaMailSender is not initialized.");
        }
        for (String recipient : recipients) {
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(sender);
                helper.setTo(recipient);
                helper.setSubject(emailDto.getSubject());
                helper.setText(emailDto.getTextBody(), emailDto.getHtmlBody());

                javaMailSender.send(mimeMessage);
            } catch (Exception e) {
                LOG.error("Failed to send email notification. {}", e.getMessage(), e);
            }
        }
    }
}
