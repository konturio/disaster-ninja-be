package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import io.kontur.disasterninja.dto.Partner;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.LayersApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class EmailNotificationService extends NotificationService {

    private final static Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailMessageFormatter emailMessageFormatter;
    private final EmailSender emailSender;
    private final LayersApiService layersApiService;
    private final GeometryTransformer geometryTransformer;

    private final ConcurrentHashMap<UUID, Long> notificationTimestamps = new ConcurrentHashMap<>();

    @Value("${notifications.emailNotificationsFrequencyMs}")
    private long emailNotificationsFrequencyMs;


    @Value("${notifications.relevantLocationsLayer}")
    private String relevantLocationsLayer;

    @Value("${notifications.relevantLocationsLayerAppId}")
    private String relevantLocationsLayerAppId;

    public EmailNotificationService(EmailMessageFormatter emailMessageFormatter,
                                    EmailSender emailSender,
                                    LayersApiService layersApiService,
                                    GeometryTransformer geometryTransformer) {
        this.emailMessageFormatter = emailMessageFormatter;
        this.emailSender = emailSender;
        this.layersApiService = layersApiService;
        this.geometryTransformer = geometryTransformer;
    }

    @Override
    public void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        LOG.info("Found new event, sending email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());

        try {
            if (!canSendNotification(event.getEventId())) {
                LOG.info("Skipped email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
                return;
            }

            List<Partner> partners = getPartners(getRelevantLocations(event.getGeometries()));
            EmailDto emailDto = emailMessageFormatter.format(event, urbanPopulationProperties, analytics, partners);
            emailSender.send(emailDto);
            LOG.info("Successfully sent email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process email notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isApplicable(EventApiEventDto event) {
        return event.getEventDetails() != null
                && event.getEpisodes() != null
                && event.getEpisodes().stream().noneMatch(episode -> episode.getEpisodeDetails() == null)
                && !CollectionUtils.isEmpty(getRelevantLocations(event.getGeometries()));
    }

    private List<Partner> getPartners(List<Feature> partnerLocations) {
        Map<String, List<String>> partnerLocationMap = partnerLocations.stream()
                .collect(Collectors.groupingBy(
                        feature -> (String) feature.getProperties().get("partner"),
                        Collectors.mapping(
                                feature -> (String) feature.getProperties().get("location"),
                                Collectors.toList())));

        return partnerLocationMap.entrySet().stream()
                .map(entry -> new Partner(entry.getKey(), entry.getValue().size(), new HashSet<>(entry.getValue())))
                .collect(Collectors.toList());
    }

    private List<Feature> getRelevantLocations(FeatureCollection fc) {
        Geometry geometry = geometryTransformer.makeValid(geometryTransformer.getGeometryFromGeoJson(fc));
        return layersApiService.getFeatures(geometry, relevantLocationsLayer, UUID.fromString(relevantLocationsLayerAppId));
    }

    private boolean canSendNotification(UUID eventId) {
        long now = System.currentTimeMillis();
        Long lastNotificationTime = notificationTimestamps.get(eventId);
        if (lastNotificationTime != null && (now - lastNotificationTime) < emailNotificationsFrequencyMs)
            return false;
        notificationTimestamps.put(eventId, now);
        return true;
    }

    /**
     * Periodically cleans up old entries from the map to save space.
     * Removes records where the last notification was sent more than the configured amount of time ago.
     */
    @Scheduled(fixedRate = 600000)
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        notificationTimestamps.entrySet().removeIf(entry -> (now - entry.getValue()) > emailNotificationsFrequencyMs);
    }
}
