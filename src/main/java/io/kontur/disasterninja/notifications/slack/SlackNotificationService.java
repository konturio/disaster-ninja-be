package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SlackNotificationService extends NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationService.class);

    private final SlackMessageFormatter slackMessageFormatter;
    private final SlackSender slackSender;
    private final String eventApiFeed;
    private final String slackWebHookUrl;

    public SlackNotificationService(SlackMessageFormatter slackMessageFormatter,
                                    SlackSender slackSender,
                                    String eventApiFeed,
                                    String slackWebHookUrl) {
        this.slackMessageFormatter = slackMessageFormatter;
        this.slackSender = slackSender;
        this.eventApiFeed = eventApiFeed;
        this.slackWebHookUrl = slackWebHookUrl;
    }

    @Override
    public void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        LOG.info("Found new event, sending slack notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());

        try {
            String message = slackMessageFormatter.format(event, urbanPopulationProperties, analytics);
            // customize formatter if needed for a specific Slack receiver
            slackSender.send(message, slackWebHookUrl);
            LOG.info("Successfully sent slack notification from feed {}. Event ID = '{}', name = '{}'", eventApiFeed, event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process slack notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }

    @Override
    public String getEventApiFeed() {
        return eventApiFeed;
    }
}
