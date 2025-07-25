package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventEpisodeListDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.kontur.disasterninja.service.converter.EventListEventDtoConverter.*;
import static java.util.Collections.emptyMap;

public class EventDtoConverter {

    private static final GeoJSONReader geoJSONReader = new GeoJSONReader();
    private static final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();

    public static EventDto convert(EventApiEventDto event) {
        EventDto dto = new EventDto();
        dto.setEventId(event.getEventId());

        dto.setEventName(eventName(event));
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setEpisodeCount(event.getEpisodeCount());
        List<String> eventUrls = event.getUrls();
        dto.setExternalUrls(eventUrls != null ? List.copyOf(eventUrls) : List.of());

        EventType eventType;
        try {
            eventType = EventType.valueOf(event.getType());
        } catch (IllegalArgumentException ex) {
            eventType = EventType.OTHER;
        }
        dto.setEventType(eventType);
        dto.setSeverity(event.getSeverity());

        dto.setMagnitude(extractMagnitude(event));
        dto.setCategory(extractCategory(event));

        if (event.getEventDetails() != null) {
            Map<String, Object> eventDetails = event.getEventDetails();
            if (eventDetails.containsKey("populatedAreaKm2")) {
                dto.setSettledArea(convertDouble(eventDetails.get("populatedAreaKm2")));
            }
            if (eventDetails.containsKey("loss")) {
                dto.setLoss(convertLong(eventDetails.get("loss")));
            }
            if (eventDetails.containsKey("population")) {
                dto.setAffectedPopulation(convertLong(eventDetails.get("population")));
            }
            if (eventDetails.containsKey("osmGapsPercentage")) {
                dto.setOsmGaps(convertLong(eventDetails.get("osmGapsPercentage")));
            }
        }

        dto.setGeojson(uniteGeometry(event));
        dto.setLatestEpisodeGeojson(event.getGeometries());
        dto.setUpdatedAt(event.getUpdatedAt());

        dto.setBbox(event.getBbox());
        dto.setCentroid(event.getCentroid());
        return dto;
    }

    public static EventEpisodeListDto convertEventEpisode(FeedEpisode episode) {
        EventEpisodeListDto result = EventEpisodeListDto.builder()
                .name(episode.getName())
                .externalUrls(episode.getUrls())
                .severity(episode.getSeverity())
                .magnitude(extractMagnitude(episode))
                .category(extractCategory(episode))
                .location(episode.getLocation())
                .startedAt(episode.getStartedAt())
                .endedAt(episode.getEndedAt())
                .updatedAt(episode.getUpdatedAt())
                .geojson(episode.getGeometries())
                .build();
        return result;
    }

    private static Double extractMagnitude(EventApiEventDto event) {
        return event.getSeverityData() != null && event.getSeverityData().containsKey("magnitude")
                ? convertDouble(event.getSeverityData().get("magnitude"))
                : null;
    }

    private static Double extractMagnitude(FeedEpisode episode) {
        return episode.getSeverityData() != null && episode.getSeverityData().containsKey("magnitude")
                ? convertDouble(episode.getSeverityData().get("magnitude"))
                : null;
    }

    private static String extractCategory(EventApiEventDto event) {
        return event.getSeverityData() != null && event.getSeverityData().containsKey("categorySaffirSimpson")
                ? convertCategory(event.getSeverityData().get("categorySaffirSimpson"))
                : null;
    }

    private static String extractCategory(FeedEpisode episode) {
        return episode.getSeverityData() != null && episode.getSeverityData().containsKey("categorySaffirSimpson")
                ? convertCategory(episode.getSeverityData().get("categorySaffirSimpson"))
                : null;
    }

    private static FeatureCollection uniteGeometry(EventApiEventDto event) {
        FeatureCollection geom = event.getGeometries();
        if (geom == null || geom.getFeatures() == null || geom.getFeatures().length == 0) {
            return new FeatureCollection(new Feature[0]);
        }
        Feature[] episodeFeatures = geom.getFeatures();
        List<Geometry> episodeGeometries = new ArrayList<>(episodeFeatures.length);
        Stream.of(episodeFeatures).forEach(f -> episodeGeometries.add(geoJSONReader.read(f.getGeometry())));

        GeometryFactory geometryFactory = new GeometryFactory(episodeGeometries.get(0).getPrecisionModel());
        org.locationtech.jts.geom.Geometry unitedGeometry = geometryFactory
                .createGeometryCollection(episodeGeometries.toArray(new org.locationtech.jts.geom.Geometry[0]))
                .union();
        return new FeatureCollection(new Feature[]{new Feature(geoJSONWriter.write(unitedGeometry), emptyMap())});
    }
}
