package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;

@Service
@RequiredArgsConstructor
public class EventShapeLayerProvider implements LayerProvider {
    private final EventApiService eventApiService;
    private final GeoJSONReader reader = new GeoJSONReader();

    /**
     * @param geoJson if specified - used to filter Event features by intersection
     * @param eventId required to get event from Event API
     */
    @Override
    public List<Layer> obtainLayers(Geometry geoJson, UUID eventId) {
        if (eventId == null) {
            return List.of();
        }
        Layer layer = fromEventDto(eventApiService.getEvent(eventId), false);
        if (layer == null) {
            return List.of();
        }

        if (layer.getSource() != null && layer.getSource().getData() != null) {
            Feature[] filteredFeatures = filterFeaturesByGeometry(layer.getSource().getData().getFeatures(), geoJson);
            layer.getSource().setData(new FeatureCollection(filteredFeatures));
        }
        return List.of(layer);
    }

    /**
     * @param layerId Layer ID
     * @param eventId required to get event from Event API
     */
    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId) || eventId == null) {
            return null;
        }
        EventDto eventDto = eventApiService.getEvent(eventId);
        return fromEventDto(eventDto, true);
    }

    Layer fromEventDto(EventDto eventDto, boolean includeSourceData) {
        if (eventDto == null) {
            return null;
        }

        //if 'Class' property is absent from features - use common config for EVENT_SHAPE_LAYER_ID
        //otherwise there are separate cfgs based on event type
        boolean isClassSpecified = Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures())
            .anyMatch(f -> f.getProperties().containsKey("Class"));
        String layerId = isClassSpecified ? EVENT_SHAPE_LAYER_ID + "." + eventDto.getEventType()
            : EVENT_SHAPE_LAYER_ID;

        Layer.LayerBuilder builder = Layer.builder()
            .id(layerId);

        if (includeSourceData) {
            builder.source(LayerSource.builder()
                .type(GEOJSON)
                .data(eventDto.getLatestEpisodeGeojson()) //sic!
                .build());
        }
        return builder.build();
    }

    @Override
    public boolean isApplicable(String layerId) {
        return EVENT_SHAPE_LAYER_ID.equals(layerId);
    }

    private org.locationtech.jts.geom.Geometry getJtsGeometry(Geometry geoJson) {
        return reader.read(geoJson);
    }

    private Feature[] filterFeaturesByGeometry(Feature[] input, Geometry geoJson) {
        if (input == null) {
            return new Feature[]{};
        }
        if (geoJson == null) {
            return input;
        }
        org.locationtech.jts.geom.Geometry jtsGeometry = getJtsGeometry(geoJson);

        //filter items by geoJson Geometry
        return Arrays.stream(input)
            .filter(json -> {
                Geometry featureGeom = json.getGeometry();
                return featureGeom == null || //include items without geometry ("global" ones)
                    jtsGeometry.intersects(reader.read(featureGeom));
            })
            .toArray(Feature[]::new);
    }
}
