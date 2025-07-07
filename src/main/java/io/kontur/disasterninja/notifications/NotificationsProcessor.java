package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class NotificationsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsProcessor.class);
    private static final Map<String, OffsetDateTime> latestUpdatedDate = new ConcurrentHashMap<>();
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

    @Value("${notifications.feed2:#{null}}")
    private String eventApiFeed2;

    public NotificationsProcessor(EventApiClient eventApiClient,
                                  InsightsApiGraphqlClient insightsApiClient,
                                  AnalyticsService analyticsService,
                                  NotificationsAnalyticsConfig notificationsAnalyticsConfig,
                                  @Nullable EmailNotificationService emailNotificationService,
                                  @Nullable SlackNotificationService slackNotificationService,
                                  @Nullable @Qualifier("slackNotificationService2") SlackNotificationService slackNotificationService2) {

        this.eventApiClient = eventApiClient;
        this.insightsApiClient = insightsApiClient;
        this.analyticsService = analyticsService;
        this.notificationsAnalyticsConfig = notificationsAnalyticsConfig;
        this.notificationServices = Stream.of(emailNotificationService, slackNotificationService, slackNotificationService2)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void run() {
        processFeed(eventApiFeed);
        if (eventApiFeed2 != null) {
            processFeed(eventApiFeed2);
        }
    }

    private void processFeed(String feed) {
        OffsetDateTime feedLatest = latestUpdatedDate.get(feed);
        if (feedLatest == null) {
            initUpdateDate(feed);
            return;
        }
        try {
            // modify parameters here for the second slack receiver if needed
            LOG.info("Requesting latest events for feed {}", feed);
            List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, feed, 100);

            for (EventApiEventDto event : events) {
                if (event.getUpdatedAt().isBefore(feedLatest)
                        || event.getUpdatedAt().isEqual(feedLatest)) {
                    break;
                }

                for (NotificationService notificationService : servicesForFeed(feed)) {
                    try {
                        if (notificationService.isApplicable(event)) {
                            Geometry geometry = convertGeometry(event.getGeometries());
                            Map<String, Object> urbanPopulationProperties = new HashMap<>();
                            Map<String, Double> analytics = new HashMap<>();
                            try {
                                urbanPopulationProperties = obtainUrbanPopulation(geometry);
                                analytics = obtainAnalytics(geometry);
                            } catch (ExecutionException | InterruptedException e) {
                                LOG.error("Failed to obtain analytics for notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
                            }
                            notificationService.process(event, urbanPopulationProperties, analytics);
                            feedLatest = event.getUpdatedAt();
                            latestUpdatedDate.put(feed, feedLatest);
                        }
                    } catch (RestClientException e) {
                        LOG.error("Notification service {} failed for event {}. {}",
                                notificationService.getClass().getSimpleName(),
                                event.getEventId(), e.getMessage(), e);
                    } catch (Exception e) {
                        LOG.error("Notification service {} failed for event {}. {}",
                                notificationService.getClass().getSimpleName(),
                                event.getEventId(), e.getMessage(), e);
                    }
                }
            }
        } catch (RestClientException e) {
            LOG.warn("Received en error while obtaining events for notification: {}", e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List<NotificationService> servicesForFeed(String feed) {
        return notificationServices.stream()
                .filter(s -> feed.equals(s.getEventApiFeed()))
                .collect(Collectors.toList());
    }

    private void initUpdateDate(String feed) {
        List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, feed, 1);
        if (events != null && events.size() > 0) {
            EventApiEventDto latestEvent = events.get(0);
            latestUpdatedDate.put(feed, latestEvent.getUpdatedAt());
        }
    }

    private Map<String, Object> obtainUrbanPopulation(
            Geometry geometry) throws ExecutionException, InterruptedException {
        Feature[] features = insightsApiClient.humanitarianImpactQuery(geometry)
                .get()
                .getFeatures();
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
        List<AnalyticsTabQuery.Function> functionsResults = analyticsService.calculateRawAnalytics(geometry,
                notificationsAnalyticsConfig.getFunctions());
        return functionsResults.stream()
                .collect(Collectors.toMap(AnalyticsTabQuery.Function::id,
                        value -> Optional.ofNullable(value.result()).orElse(0.0)));
    }

    private Geometry convertGeometry(FeatureCollection shape) {
        if (shape == null) {
            return null;
        }
        org.wololo.geojson.Geometry[] geometries = Stream.of(shape.getFeatures())
                .map(Feature::getGeometry)
                .toArray(org.wololo.geojson.Geometry[]::new);
        GeometryCollection geometryCollection = new GeometryCollection(geometries);
        return geoJSONWriter.write(geoJSONReader.read(geometryCollection).union());
    }

}
