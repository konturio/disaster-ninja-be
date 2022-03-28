package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.GeometryCollection;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@Component
@ConditionalOnProperty(value="notifications.enabled")
public class NotificationsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsProcessor.class);
    private static volatile OffsetDateTime latestUpdatedDate = null;
    private static final GeoJSONReader geoJSONReader = new GeoJSONReader();
    private static final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();
    private static final List<String> acceptableTypes = Arrays.asList("FLOOD", "EARTHQUAKE", "CYCLONE", "VOLCANO", "WILDFIRE");

    private final EventApiClient eventApiClient;
    private final InsightsApiGraphqlClient insightsApiClient;
    private final SlackMessageFormatter slackMessageFormatter;
    private final AnalyticsService analyticsService;
    private final NotificationsAnalyticsConfig notificationsAnalyticsConfig;

    @Value("${notifications.slackWebHook:}")
    private String slackWebHookUrl;

    public NotificationsProcessor(SlackMessageFormatter slackMessageFormatter,
                                  EventApiClient eventApiClient,
                                  InsightsApiGraphqlClient insightsApiClient,
                                  AnalyticsService analyticsService,
                                  NotificationsAnalyticsConfig notificationsAnalyticsConfig) {

        this.slackMessageFormatter = slackMessageFormatter;
        this.eventApiClient = eventApiClient;
        this.insightsApiClient = insightsApiClient;
        this.analyticsService = analyticsService;
        this.notificationsAnalyticsConfig = notificationsAnalyticsConfig;
    }

    @PostConstruct
    public void init() {
        List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, 1);
        EventApiEventDto latestEvent = events.get(0);
        latestUpdatedDate = latestEvent.getUpdatedAt();
    }

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void run() {
        try {
            List<EventApiEventDto> events = eventApiClient.getLatestEvents(acceptableTypes, 100);

            for (EventApiEventDto event : events) {
                if (event.getUpdatedAt().isBefore(latestUpdatedDate)
                        || event.getUpdatedAt().isEqual(latestUpdatedDate)) {
                    break;
                }

                if (isEventInPopulatedArea(event) && isEventTypeAppropriate(event)) {
                    FeedEpisode latestEpisode = event.getEpisodes().get(0);
                    Geometry geometry = convertGeometry(latestEpisode.getGeometries());
                    latestUpdatedDate = event.getUpdatedAt();
                    process(event, geometry);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void process(EventApiEventDto event, Geometry geometry) {
        LOG.info("New event has been occurred. Sending notifications. Event ID = '{}', name = '{}'",
                event.getEventId(), event.getName());

        Map<String, Object> urbanPopulationProperties = new HashMap<>();
        Map<String, Double> analytics = new HashMap<>();
        try {
            urbanPopulationProperties = obtainUrbanPopulation(geometry);
            analytics = obtainAnalytics(geometry);
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn(e.getMessage(), e);
        }

        String message = slackMessageFormatter.format(event, urbanPopulationProperties, analytics);
        sendNotification(message);

        LOG.info("Notifications were sent.");
    }

    /**
     * Hotfix for Industrial heats being wildfires #7985
     */
    private boolean isEventTypeAppropriate(EventApiEventDto eventApiEventDto) {
        return acceptableTypes.contains(eventApiEventDto.getEpisodes().get(0).getType());
    }

    private boolean isEventInPopulatedArea(EventApiEventDto event) {
        if (event.getEpisodes().get(0).getEpisodeDetails() == null) {
            return false;
        }
        String population = String.valueOf(event.getEpisodes().get(0).getEpisodeDetails().get("population"));
        return population != null &&
                new BigDecimal(population).compareTo(new BigDecimal(500)) >= 0;
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

    private void sendNotification(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            new RestTemplate().exchange(slackWebHookUrl, HttpMethod.POST,
                    new HttpEntity<>(message, headers), String.class);
        } catch (Exception e) {
            LOG.error("Unexpected error when sending notifications", e);
        }
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
