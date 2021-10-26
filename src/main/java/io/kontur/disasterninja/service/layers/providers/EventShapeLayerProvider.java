package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.RASTER;

@Service
public class EventShapeLayerProvider implements LayerProvider {
    @Autowired
    EventApiService eventApiService;
    private GeoJSONReader reader = new GeoJSONReader();

    @Override
    public List<Layer> obtainLayers(Geometry geoJson, UUID eventId) {
        Layer layer = obtainLayer(EVENT_SHAPE_LAYER_ID, eventId);
        if (layer == null) {
            return Collections.emptyList();
        }

        if (layer.getSource() != null && layer.getSource().getData() != null) {
            Feature[] filteredFeatures = filterFeaturesByGeometry(layer.getSource().getData().getFeatures(), geoJson);
            layer.getSource().setData(new FeatureCollection(filteredFeatures));
        }
        return List.of(layer);
    }

    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        EventDto eventDto = eventApiService.getEvent(eventId);
        return fromEventDto(eventDto);
    }

    Layer fromEventDto(EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }
        return Layer.builder()
            .id(EVENT_SHAPE_LAYER_ID)
            .source(LayerSource.builder()
                .type(RASTER) //todo check
//                .url() //todo other fields
//                .tileSize()
                .data(eventDto.getGeojson())
                .build())
            .build();
    }

    @Override
    public boolean isApplicable(String layerId) {
        return EVENT_SHAPE_LAYER_ID.equals(layerId);
    }

    protected org.locationtech.jts.geom.Geometry getJtsGeometry(Geometry geoJson) {
        return reader.read(geoJson);
    }

    private Feature[] filterFeaturesByGeometry(Feature[] input, Geometry geoJson) {
        if (input == null) {
            return new Feature[]{};
        }
        org.locationtech.jts.geom.Geometry jtsGeometry = getJtsGeometry(geoJson);

        //filter items by geoJson Geometry
        return Arrays.stream(input)
            .filter(json -> {
                Geometry featureGeom = json.getGeometry();
                return featureGeom == null || //include items without geometry ("global" ones) //todo seems to be unneeded here?
                    jtsGeometry.intersects(reader.read(featureGeom));
            })
            .toArray(Feature[]::new);
    }
}
