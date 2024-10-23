package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class EmailNotificationService extends NotificationService {

    private final static Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailMessageFormatter emailMessageFormatter;
    private final EmailSender emailSender;

    public EmailNotificationService(EmailMessageFormatter emailMessageFormatter, EmailSender emailSender) {
        this.emailMessageFormatter = emailMessageFormatter;
        this.emailSender = emailSender;
    }

    @Override
    public void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        LOG.info("Found new event, sending email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());

        try {
            EmailDto emailDto = emailMessageFormatter.format(event, urbanPopulationProperties, analytics);
            emailSender.send(emailDto);
        } catch (Exception e) {
            LOG.error("Failed to process email notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
        LOG.info("Successfully sent email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
    }
}
