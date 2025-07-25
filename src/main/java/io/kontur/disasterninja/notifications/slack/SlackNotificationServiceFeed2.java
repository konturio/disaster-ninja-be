package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.Severity;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.service.converter.GeometryConverter;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.util.CountryBoundaryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.time.ZoneOffset.UTC;

public class SlackNotificationServiceFeed2 extends SlackNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackNotificationServiceFeed2.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'");
    private volatile Geometry usBoundary;
    private final LayersApiClient layersApiClient;

    public SlackNotificationServiceFeed2(SlackMessageFormatter slackMessageFormatter,
                                         SlackSender slackSender,
                                         String eventApiFeed,
                                         String slackWebHookUrl,
                                         Geometry usBoundary,
                                         LayersApiClient layersApiClient) {
        super(slackMessageFormatter, slackSender, eventApiFeed, slackWebHookUrl);
        this.usBoundary = usBoundary;
        this.layersApiClient = layersApiClient;
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

    public static String severityDataToCategory(Map<String, Object> severityData) {
        Set<String> allowedKeys = Set.of(
            "depthKm", "magnitude", "windSpeedKph", "categorySaffirSimpson",
            "burnedAreaKm2", "containedAreaPct", "depth", "maxpga"
        );

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : severityData.entrySet()) {
            if (allowedKeys.contains(entry.getKey())) {
                if (result.length() > 0) result.append(", ");
                result.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return result.toString();
    }

    private String buildHeader(EventApiEventDto event) {
        StringBuilder header = new StringBuilder();
        header.append("Event Type: ")
              .append(StringUtils.capitalize(String.valueOf(event.getType()).toLowerCase()))
              .append("\n");

        Map<String, Object> severityData = event.getSeverityData();
        if (severityData != null && !severityData.isEmpty()) {
            String category = severityDataToCategory(severityData);
            header.append("Category: ").append(category).append("\n");
        }

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
        Geometry eventGeometry = GeometryConverter.convertGeometry(event.getGeometries());

        if (eventGeometry == null) {
            return false;
        }

        if (usBoundary == null) {
            LOG.warn("US boundary is null - attempting reload");
            usBoundary = CountryBoundaryUtil.loadCountryBoundary(layersApiClient, "usa");
            if (usBoundary == null) {
                LOG.warn("US boundary is still null - event will be skipped");
                return false;
            }
        }

        if (!usBoundary.intersects(eventGeometry)) {
            return false;
        }

        String type = event.getType();
        Map<String, Object> severityData = event.getSeverityData() == null ? Collections.emptyMap() : event.getSeverityData();
        Map<String, Object> eventDetails = event.getEventDetails() == null ? Collections.emptyMap() : event.getEventDetails();
        Map<String, Object> episodeDetails = Collections.emptyMap();
        if (event.getEpisodes() != null && !event.getEpisodes().isEmpty()) {
            FeedEpisode episode = event.getEpisodes().get(0);
            if (episode.getEpisodeDetails() != null) {
                episodeDetails = episode.getEpisodeDetails();
            }
        }

        switch (type) {
            case "CYCLONE":
                Double category = toDouble(severityData.get("categorySaffirSimpson"));
                return category != null && category >= 3;
            case "EARTHQUAKE":
                Double magnitude = toDouble(severityData.get("magnitude"));
                Double pga = toDouble(severityData.get("maxpga"));
                return (magnitude != null && magnitude >= 7.5) || (pga != null && pga >= .4);
            case "WILDFIRE":
                Double km2 = toDouble(severityData.get("burnedAreaKm2"));
                return km2 != null && km2 * 247.105 >= 15000;  // fire area is ≥ 15,000 acres
            case "FLOOD":
                if (episodeDetails.isEmpty()) {
                    return false;
                }
                Double population = toDouble(episodeDetails.get("population"));
                Severity s = event.getSeverity();
                return s != null && (s == Severity.MODERATE || s == Severity.SEVERE || s == Severity.EXTREME) && population != null && population > 0;
            default:
                return false;
        }
    }

    private static Double toDouble(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
