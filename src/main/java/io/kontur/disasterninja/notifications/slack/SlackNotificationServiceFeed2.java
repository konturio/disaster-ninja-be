package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.converter.GeometryConverter;
import io.kontur.disasterninja.notifications.NotificationsProcessor;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.ZoneOffset.UTC;

public class SlackNotificationServiceFeed2 extends SlackNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationServiceFeed2.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'");
    private final Geometry usBoundary;

    public SlackNotificationServiceFeed2(SlackMessageFormatter slackMessageFormatter,
                                         SlackSender slackSender,
                                         String eventApiFeed,
                                         String slackWebHookUrl,
                                         Geometry usBoundary) {
        super(slackMessageFormatter, slackSender, eventApiFeed, slackWebHookUrl);
        this.usBoundary = usBoundary;
    }

    @Override
    protected String formatMessage(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        String header = buildHeader(event);
        String eventIdLine = "event_id: " + event.getEventId();

        String color = slackMessageFormatter.getColorCode(event, true);
        String status = slackMessageFormatter.getStatus(event);
        String title = "> " + color + status + SlackMessageFormatter.sanitizeEventName(event.getName());
        String description = slackMessageFormatter.buildDescription(event, urbanPopulationProperties, analytics, false);
        String text = header + "\n" + eventIdLine + "\n" + title + description;
        return slackMessageFormatter.wrapPlain(text);
    }

    private String buildHeader(EventApiEventDto event) {
        StringBuilder header = new StringBuilder();
        header.append("Event Type: ")
              .append(StringUtils.capitalize(String.valueOf(event.getType()).toLowerCase()))
              .append("\n");

        // TODO: get severityData and add Category line

        if (StringUtils.isNotBlank(event.getLocation())) {
            header.append("Location: ").append(event.getLocation()).append("\n");
        }

        if (event.getStartedAt() != null) {
            header.append("Event Start Date: ")
                  .append(event.getStartedAt().withOffsetSameInstant(UTC).format(DATE_FORMATTER))
                  .append("\n");
        }

        header.append("Status: ")
              .append(event.getVersion() == 1 ? "New" : "Update");

        return header.toString();
    }

    @Override
    public boolean isApplicable(EventApiEventDto event) {
        Geometry eventGeometry = convertGeometry(event.getGeometries());

        if (eventGeometry == null) {
            LOG.warn("Event geometry is null for event {}", event.getName());
            return false;
        }

        if (usBoundary == null) {
            LOG.warn("US boundary is null - all events will be filtered out");
            return false;
        }

        boolean intersects = usBoundary.intersects(eventGeometry);
        LOG.info("Event '{}' intersects US boundary: {}", event.getName(), intersects);
        return intersects;
    }

    private Geometry convertGeometry(FeatureCollection shape) {
        if (shape == null) {
            return null;
        }
        org.wololo.geojson.Geometry geo = NotificationsProcessor.convertGeometry(shape);
        return GeometryConverter.getJtsGeometry(geo);
    }

}
