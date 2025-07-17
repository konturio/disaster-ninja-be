package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.ZoneOffset.UTC;

/**
 * Slack notification service for feed2 that ignores applicability checks and
 * omits event links in the message.
 */
public class SlackNotificationServiceFeed2 extends SlackNotificationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'");

    public SlackNotificationServiceFeed2(SlackMessageFormatter slackMessageFormatter,
                                         SlackSender slackSender,
                                         String eventApiFeed,
                                         String slackWebHookUrl) {
        super(slackMessageFormatter, slackSender, eventApiFeed, slackWebHookUrl);
    }

    @Override
    protected String formatMessage(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        StringBuilder header = new StringBuilder();
        header.append("Event Type: ")
              .append(WordUtils.capitalizeFully(String.valueOf(event.getType())))
              .append("\n");

        String category = extractCategory(event);
        if (StringUtils.isNotBlank(category)) {
            header.append("Category: ").append(category).append("\n");
        }

        if (StringUtils.isNotBlank(event.getLocation())) {
            header.append("Location: ").append(event.getLocation()).append("\n");
        }

        if (event.getStartedAt() != null) {
            header.append("Event Time Date: ")
                  .append(event.getStartedAt().withOffsetSameInstant(UTC).format(DATE_FORMATTER))
                  .append("\n");
        }

        header.append("Update Status: ")
              .append(event.getVersion() == 1 ? "New" : "Update")
              .append("\n");
        header.append("event_id: ").append(event.getEventId());

        String description = slackMessageFormatter.buildDescription(event, urbanPopulationProperties, analytics, false);
        String text = header.append(description).toString();
        return slackMessageFormatter.wrapPlain(text);
    }

    private String extractCategory(EventApiEventDto event) {
        if (event.getSeverityData() == null) {
            return "";
        }
        Object value = event.getSeverityData().get("severitytext");
        if (value == null) {
            value = event.getSeverityData().get("severityText");
        }
        return value != null ? String.valueOf(value) : "";
    }

    @Override
    public boolean isApplicable(EventApiEventDto event) {
        return true;
    }

    // use inherited getEventApiFeed()
}
