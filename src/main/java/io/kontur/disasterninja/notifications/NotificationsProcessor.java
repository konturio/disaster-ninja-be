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
import io.kontur.disasterninja.util.CountryBoundaryUtil;
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

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class NotificationsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsProcessor.class);
    private static final Map<String, OffsetDateTime> latestUpdatedDate = new ConcurrentHashMap<>();
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
        this.usBoundary = CountryBoundaryUtil.loadCountryBoundary(layersApiClient, "usa");
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
                            org.locationtech.jts.geom.Geometry geometry = GeometryConverter.convertGeometry(event.getGeometries());
                            org.wololo.geojson.Geometry geoJson = GeometryConverter.toGeoJson(geometry);
                            Map<String, Object> urbanPopulationProperties = new HashMap<>();
                            Map<String, Double> analytics = new HashMap<>();
                            try {
                                urbanPopulationProperties = obtainUrbanPopulation(geoJson);
                                analytics = obtainAnalytics(geoJson);
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
                    eventApiFeed2, slackWebHook2, usBoundary, layersApiClient));
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
            org.wololo.geojson.Geometry geometry) throws ExecutionException, InterruptedException {
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

    private Map<String, Double> obtainAnalytics(org.wololo.geojson.Geometry geometry) {
        List<AnalyticsTabQuery.Function> functionsResults = analyticsService.calculateRawAnalytics(geometry,
                notificationsAnalyticsConfig.getFunctions());
        return functionsResults.stream()
                .collect(Collectors.toMap(AnalyticsTabQuery.Function::id,
                        value -> Optional.ofNullable(value.result()).orElse(0.0)));
    }

}
