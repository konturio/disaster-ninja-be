package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.service.AnalyticsService;
import io.kontur.disasterninja.notifications.email.EmailNotificationService;
import io.kontur.disasterninja.notifications.slack.SlackMessageFormatter;
import io.kontur.disasterninja.notifications.slack.SlackSender;
import io.kontur.disasterninja.notifications.slack.SlackNotificationService;
import io.kontur.disasterninja.notifications.slack.SlackNotificationServiceFeed2;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.service.converter.GeometryConverter;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final EmailNotificationService emailNotificationService;
    private final SlackMessageFormatter slackMessageFormatter;
    private final SlackSender slackSender;
    private final LayersApiClient layersApiClient;
    private final org.locationtech.jts.geom.Geometry usBoundary;

    @Value("${notifications.feed}")
    private String eventApiFeed;

    @Value("${notifications.feed2:#{null}}")
    private String eventApiFeed2;

    @Value("${notifications.slackWebHook:}")
    private String slackWebHook;

    @Value("${notifications.slackWebHook2:}")
    private String slackWebHook2;

    public NotificationsProcessor(EventApiClient eventApiClient,
                                  InsightsApiGraphqlClient insightsApiClient,
                                  AnalyticsService analyticsService,
                                  NotificationsAnalyticsConfig notificationsAnalyticsConfig,
                                  @Autowired(required = false) EmailNotificationService emailNotificationService,
                                  SlackMessageFormatter slackMessageFormatter,
                                  SlackSender slackSender,
                                  LayersApiClient layersApiClient) {

        this.eventApiClient = eventApiClient;
        this.insightsApiClient = insightsApiClient;
        this.analyticsService = analyticsService;
        this.notificationsAnalyticsConfig = notificationsAnalyticsConfig;
        this.emailNotificationService = emailNotificationService;
        this.slackMessageFormatter = slackMessageFormatter;
        this.slackSender = slackSender;
        this.layersApiClient = layersApiClient;
        this.usBoundary = loadUsBoundary("usa");
    }

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void run() {
        processFeed(eventApiFeed);
        if (eventApiFeed2 != null && !"".equalsIgnoreCase(eventApiFeed2) && !"none".equalsIgnoreCase(eventApiFeed2)) {
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
            LOG.info("Requesting latest events for feed {} types {}", feed, acceptableTypes);
            List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, feed, 100);


            List<NotificationService> feedServices = servicesForFeed(feed);

            for (EventApiEventDto event : events) {
                if (feedLatest.isAfter(event.getUpdatedAt())
                        || feedLatest.isEqual(event.getUpdatedAt())) {
                    LOG.info("No new events for feed {}. Latest processed: {}, current event: {}", feed, feedLatest, event.getUpdatedAt());
                    break;
                }

                for (NotificationService notificationService : feedServices) {
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

                // update last processed timestamp even if event is not applicable
                feedLatest = event.getUpdatedAt();
                latestUpdatedDate.put(feed, feedLatest);
            }
        } catch (RestClientException e) {
            LOG.warn("Received en error while obtaining events for notification: {}", e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List<NotificationService> servicesForFeed(String feed) {
        List<NotificationService> result = new ArrayList<>();

        if (feed.equals(eventApiFeed)) {
            if (emailNotificationService != null) {
                result.add(emailNotificationService);
            }
            // first slack receiver uses default parameters
            result.add(new SlackNotificationService(slackMessageFormatter, slackSender, eventApiFeed, slackWebHook));
        }

        if (feed.equals(eventApiFeed2)) {
            // second Slack receiver sends all events without filters and without links
            result.add(new SlackNotificationServiceFeed2(slackMessageFormatter, slackSender,
                    eventApiFeed2, slackWebHook2, usBoundary));
        }

        return result;
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

    public static Geometry convertGeometry(FeatureCollection shape) {
        if (shape == null) {
            return null;
        }
        org.wololo.geojson.Geometry[] geometries = Stream.of(shape.getFeatures())
                .map(Feature::getGeometry)
                .toArray(org.wololo.geojson.Geometry[]::new);
        GeometryCollection geometryCollection = new GeometryCollection(geometries);
        return geoJSONWriter.write(geoJSONReader.read(geometryCollection).union());
    }

    private org.locationtech.jts.geom.Geometry loadUsBoundary(String iso3Code) {
        try {
            FeatureCollection fc = layersApiClient.getCountryBoundary(iso3Code);
            if (fc == null || fc.getFeatures() == null || fc.getFeatures().length == 0) {
                return null;
            }
            org.wololo.geojson.Geometry geo = convertGeometry(fc);
            return GeometryConverter.getJtsGeometry(geo);
        } catch (Exception e) {
            LOG.error("Failed to load {} boundary: {}", iso3Code.toUpperCase(), e.getMessage(), e);
            return null;
        }
    }

}
