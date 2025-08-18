package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SlackNotificationService extends NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationService.class);

    /**
     * Remember the time of the last sent message per event ID to avoid
     * sending duplicate Slack notifications for the same event within a
     * configurable time window.
     */
    private static final Map<UUID, Instant> LAST_SENT = new ConcurrentHashMap<>();

    private static final Duration RESEND_INTERVAL = Duration.ofDays(1);

    protected final SlackMessageFormatter slackMessageFormatter;
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
            Instant now = Instant.now();

            if (isDeduplicationEnabled()) {
                Instant lastSent = LAST_SENT.get(event.getEventId());
                if (lastSent != null && now.isBefore(lastSent.plus(RESEND_INTERVAL))) {
                    LOG.info("Skipping slack notification for event '{}' as it was sent less than {} ago", event.getEventId(), RESEND_INTERVAL);
                    return;
                }
            }

            String message = formatMessage(event, urbanPopulationProperties, analytics);

            // customize formatter if needed for a specific Slack receiver
            slackSender.send(message, slackWebHookUrl);

            if (isDeduplicationEnabled()) {
                LAST_SENT.put(event.getEventId(), now);
            }

            LOG.info("Successfully sent slack notification from feed {}. Event ID = '{}', name = '{}'", eventApiFeed, event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process slack notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }

    protected String formatMessage(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        return slackMessageFormatter.format(event, urbanPopulationProperties, analytics);
    }

    @Override
    public String getEventApiFeed() {
        return eventApiFeed;
    }

    /**
     * Allows subclasses to disable deduplication logic.
     */
    protected boolean isDeduplicationEnabled() {
        return true;
    }
}
