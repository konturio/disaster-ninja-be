package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.notifications.NotificationService;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.LayersApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class EmailNotificationService extends NotificationService {

    private final static Logger LOG = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailMessageFormatter emailMessageFormatter;
    private final EmailSender emailSender;
    private final LayersApiService layersApiService;
    private final GeometryTransformer geometryTransformer;

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
            EmailDto emailDto = emailMessageFormatter.format(event, urbanPopulationProperties, analytics);
            emailSender.send(emailDto);
            LOG.info("Successfully sent email notification. Event ID = '{}', name = '{}'", event.getEventId(), event.getName());
        } catch (Exception e) {
            LOG.error("Failed to process email notification. Event ID = '{}', name = '{}'. {}", event.getEventId(), event.getName(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isApplicable(EventApiEventDto event) {
        Geometry geometry = geometryTransformer.makeValid(geometryTransformer.getGeometryFromGeoJson(event.getGeometries()));
        List<Feature> features = layersApiService.getFeatures(geometry, relevantLocationsLayer, UUID.fromString(relevantLocationsLayerAppId));
        return !CollectionUtils.isEmpty(features);
    }
}
