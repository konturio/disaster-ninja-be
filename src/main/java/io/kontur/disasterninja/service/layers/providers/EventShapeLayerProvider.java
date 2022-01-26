package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.config.logging.LogHttpTraceRepository.LOG;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getJtsGeometry;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getPreparedGeometryFromRequest;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class EventShapeLayerProvider implements LayerProvider {
    private final EventApiService eventApiService;

    /**
     * @param geoJson if specified - used to filter Event features by intersection
     * @param eventId required to get event from Event API
     */
    @Override
    public List<Layer> obtainLayers(Geometry geoJson, UUID eventId) {
        if (eventId == null) {
            return null;
        }
        Layer layer = obtainLayer(geoJson, EVENT_SHAPE_LAYER_ID, eventId);
        return layer == null ? null : List.of(layer);
    }

    /**
     * @param geoJSON if specified - used to filter Event features by intersection
     * @param layerId Layer ID
     * @param eventId required to get event from Event API
     */
    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (eventId == null) {
            throw new WebApplicationException("EventId must be provided when requesting layer " + layerId,
                HttpStatus.BAD_REQUEST);
        }
        EventDto eventDto = eventApiService.getEvent(eventId, null, null); //todo either userToken and feedname should be provided in request or default feed and auth should be used?

        Layer layer = fromEventDto(eventDto);
        if (layer != null && layer.getSource() != null && layer.getSource().getData() != null) {
            Feature[] filteredFeatures = filterFeaturesByGeometry(layer.getSource().getData().getFeatures(), geoJSON);
            if (filteredFeatures.length == 0) {
                LOG.info("No features intersecting with requested boundary {}", geoJSON);
                return null;
            }
            layer.getSource().setData(new FeatureCollection(filteredFeatures));
        }
        return layer;
    }

    Layer fromEventDto(EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }

        //if 'Class' property is absent from features - use common config for EVENT_SHAPE_LAYER_ID
        //otherwise there are separate cfgs based on event type
        boolean isClassSpecified = Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures())
            .anyMatch(f -> f.getProperties().containsKey("Class"));
        String layerId = isClassSpecified ? EVENT_SHAPE_LAYER_ID + "." + eventDto.getEventType()
            : EVENT_SHAPE_LAYER_ID;

        return Layer.builder()
            .id(layerId)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(eventDto.getLatestEpisodeGeojson()) //sic!
                .build())
            .build();
    }

    @Override
    public boolean isApplicable(String layerId) {
        return EVENT_SHAPE_LAYER_ID.equals(layerId);
    }

    private Feature[] filterFeaturesByGeometry(Feature[] input, Geometry geoJson) {
        if (input == null) {
            return new Feature[]{};
        }
        if (geoJson == null) {
            return input;
        }
        PreparedGeometry jtsGeometry = getPreparedGeometryFromRequest(geoJson);

        //filter items by geoJson Geometry
        return Arrays.stream(input)
            .filter(json -> {
                Geometry featureGeom = json.getGeometry();
                return featureGeom == null || //include items without geometry ("global" ones)
                    jtsGeometry.intersects(getJtsGeometry(featureGeom));
            })
            .toArray(Feature[]::new);
    }
}
