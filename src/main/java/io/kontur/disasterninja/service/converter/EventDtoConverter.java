package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.kontur.disasterninja.service.converter.EventListEventDtoConverter.convertDouble;
import static io.kontur.disasterninja.service.converter.EventListEventDtoConverter.eventName;
import static java.util.Collections.emptyMap;

public class EventDtoConverter {

    private static final GeoJSONReader geoJSONReader = new GeoJSONReader();
    private static final GeoJSONWriter geoJSONWriter = new GeoJSONWriter();

    public static EventDto convert(EventApiEventDto event) {
        EventDto dto = new EventDto();
        dto.setEventId(event.getEventId());

        dto.setEventName(eventName(event));
        dto.setLocation(event.getLocation());
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

        if (event.getEventDetails() != null) {
            dto.setSettledArea(convertDouble(event.getEventDetails().get("populatedAreaKm2")));
        } else {
            dto.setSettledArea(0d);
        }

        dto.setGeojson(uniteGeometry(event)); //todo isn't this event's geometries? -- check in eventApi!
        dto.setLatestEpisodeGeojson(event.getGeometries());

        return dto;
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
