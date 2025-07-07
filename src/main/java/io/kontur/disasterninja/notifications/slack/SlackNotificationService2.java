package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Slack notification receiver for events from second feed.
 * To customize message format, provide another formatter implementation and wire it here.
 */
@Component
@ConditionalOnProperty(value = "notifications.slack2.enabled")
public class SlackNotificationService2 extends NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationService2.class);

    private final SlackMessageFormatter slackMessageFormatter;
    private final SlackSender slackSender;

    @Value("${notifications.feed2}")
    private String eventApiFeed;

    @Value("${notifications.slackWebHook2:}")
    private String slackWebHookUrl;

    public SlackNotificationService2(SlackMessageFormatter slackMessageFormatter,
                                     SlackSender slackSender) {
        this.slackMessageFormatter = slackMessageFormatter;
        this.slackSender = slackSender;
    }

    @Override
    public void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        LOG.info("Found new event for second Slack, sending notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
        try {
            String message = slackMessageFormatter.format(event, urbanPopulationProperties, analytics);
            // Change slackWebHookUrl here if custom routing is needed
            slackSender.send(message, slackWebHookUrl);
            LOG.info("Successfully sent second slack notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process second slack notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }

    @Override
    public String getEventApiFeed() {
        return eventApiFeed;
    }
}
