package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(value = "notifications.slack.enabled")
public class SlackNotificationService extends NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationService.class);

    private final SlackMessageFormatter slackMessageFormatter;
    private final SlackSender slackSender;

    public SlackNotificationService(SlackMessageFormatter slackMessageFormatter,
                                    SlackSender slackSender) {
        this.slackMessageFormatter = slackMessageFormatter;
        this.slackSender = slackSender;
    }

    @Override
    public void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        LOG.info("Found new event, sending slack notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());

        try {
            String message = slackMessageFormatter.format(event, urbanPopulationProperties, analytics);
            slackSender.send(message);
            LOG.info("Successfully sent slack notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process slack notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }
}
