package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.GeometryCollection;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class NotificationsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsProcessor.class);
    private static volatile OffsetDateTime latestUpdatedDate = null;
    private static final GeoJSONReader geoJSONReader = new GeoJSONReader();
    private static final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();
    private static final List<String> acceptableTypes = Arrays.asList("FLOOD", "EARTHQUAKE", "CYCLONE", "VOLCANO",
            "WILDFIRE");

    private final EventApiClient eventApiClient;
    private final InsightsApiGraphqlClient insightsApiClient;
    private final AnalyticsService analyticsService;
    private final NotificationsAnalyticsConfig notificationsAnalyticsConfig;
    private final List<NotificationService> notificationServices;

    @Value("${notifications.feed}")
    private String eventApiFeed;

    public NotificationsProcessor(EventApiClient eventApiClient,
                                  InsightsApiGraphqlClient insightsApiClient,
                                  AnalyticsService analyticsService,
                                  NotificationsAnalyticsConfig notificationsAnalyticsConfig,
                                  List<NotificationService> notificationServices) {

        this.eventApiClient = eventApiClient;
        this.insightsApiClient = insightsApiClient;
        this.analyticsService = analyticsService;
        this.notificationsAnalyticsConfig = notificationsAnalyticsConfig;
        this.notificationServices = notificationServices;
    }

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void run() {
        LOG.info("Notifications processor tick. Latest processed event time: {}", latestUpdatedDate);
        if (latestUpdatedDate == null) {
            LOG.info("Latest update date is null. Initializing on first run");
            initUpdateDate();
            return;
        }
        try {
            LOG.info("Requesting latest events from Event API. Feed: {}, types: {}", eventApiFeed, acceptableTypes);
            List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, eventApiFeed, 100);
            LOG.info("Event API returned {} events", events == null ? 0 : events.size());

            for (EventApiEventDto event : events) {
                LOG.info("Considering event {} updated at {}", event.getEventId(), event.getUpdatedAt());
                if (event.getUpdatedAt().isBefore(latestUpdatedDate)
                        || event.getUpdatedAt().isEqual(latestUpdatedDate)) {
                    LOG.info("Event {} is older than last processed event. Stopping iteration", event.getEventId());
                    break;
                }

                for (NotificationService notificationService : notificationServices) {
                    LOG.info("Checking applicability for service {} on event {}", notificationService.getClass().getSimpleName(), event.getEventId());
                    if (notificationService.isApplicable(event)) {
                        LOG.info("Service {} applicable. Preparing analytics", notificationService.getClass().getSimpleName());
                        Geometry geometry = convertGeometry(event.getGeometries());
                        Map<String, Object> urbanPopulationProperties = new HashMap<>();
                        Map<String, Double> analytics = new HashMap<>();
                        try {
                            urbanPopulationProperties = obtainUrbanPopulation(geometry);
                            LOG.info("Obtained urban population analytics: {}", urbanPopulationProperties);
                            analytics = obtainAnalytics(geometry);
                            LOG.info("Obtained additional analytics: {}", analytics);
                        } catch (ExecutionException | InterruptedException e) {
                            LOG.error("Failed to obtain analytics for notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
                        }
                        try {
                            notificationService.process(event, urbanPopulationProperties, analytics);
                        } catch (Exception e) {
                            LOG.error("Notification service {} failed for event {}. {}",
                                    notificationService.getClass().getSimpleName(),
                                    event.getEventId(), e.getMessage(), e);
                            // continue with the next service
                            continue;
                        }
                        latestUpdatedDate = event.getUpdatedAt();
                        LOG.info("Event {} processed. Latest processed time updated to {}", event.getEventId(), latestUpdatedDate);
                    } else {
                        LOG.info("Service {} not applicable for event {}", notificationService.getClass().getSimpleName(), event.getEventId());
                    }
                }
            }
        } catch (RestClientException e) {
            LOG.warn("Received en error while obtaining events for notification: {}", e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initUpdateDate() {
        LOG.info("Initializing latest update date from Event API");
        List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, eventApiFeed, 1);
        if (events != null && events.size() > 0) {
            EventApiEventDto latestEvent = events.get(0);
            latestUpdatedDate = latestEvent.getUpdatedAt();
            LOG.info("Initialized latest update date as {} from event {}", latestUpdatedDate, latestEvent.getEventId());
        } else {
            LOG.info("Event API returned no events when initializing latest update date");
        }
    }

    private Map<String, Object> obtainUrbanPopulation(
            Geometry geometry) throws ExecutionException, InterruptedException {
        LOG.info("Obtaining urban population data for geometry");
        Feature[] features = insightsApiClient.humanitarianImpactQuery(geometry)
                .get()
                .getFeatures();
        LOG.info("Urban population query returned {} features", features == null ? 0 : features.length);
        if (features == null || features.length == 0) {
            return emptyMap();
        }
        return Arrays.stream(features)
                .map(Feature::getProperties)
                .filter(props -> "Kontur Urban Core".equals(props.get("name")))
                .findFirst()
                .orElseGet(Collections::emptyMap);
    }

    private Map<String, Double> obtainAnalytics(Geometry geometry) {
        LOG.info("Calculating analytics for geometry");
        List<AnalyticsTabQuery.Function> functionsResults = analyticsService.calculateRawAnalytics(geometry,
                notificationsAnalyticsConfig.getFunctions());
        LOG.info("Analytics service returned {} function results", functionsResults == null ? 0 : functionsResults.size());
        return functionsResults.stream()
                .collect(Collectors.toMap(AnalyticsTabQuery.Function::id,
                        value -> Optional.ofNullable(value.result()).orElse(0.0)));
    }

    private Geometry convertGeometry(FeatureCollection shape) {
        if (shape == null) {
            return null;
        }
        LOG.info("Converting {} geometries to unified geometry", shape.getFeatures() == null ? 0 : shape.getFeatures().length);
        org.wololo.geojson.Geometry[] geometries = Stream.of(shape.getFeatures())
                .map(Feature::getGeometry)
                .toArray(org.wololo.geojson.Geometry[]::new);
        GeometryCollection geometryCollection = new GeometryCollection(geometries);
        return geoJSONWriter.write(geoJSONReader.read(geometryCollection).union());
    }

}
