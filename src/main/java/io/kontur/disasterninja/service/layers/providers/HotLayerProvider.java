package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.client.KcApiClient.HOT_PROJECTS;
import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.layers.providers.OsmLayerProvider.getFeatureProperty;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HotLayerProvider implements LayerProvider {

    private final KcApiClient kcApiClient;

    /**
     * @param geoJSON required to find features by intersection
     * @param eventId not used
     */
    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        if (geoJSON == null) {
            return List.of();
        }
        Layer layer = obtainLayer(geoJSON, HOT_LAYER_ID, eventId);
        return layer == null ? List.of() : List.of(layer);
    }

    /**
     * @return null
     */
    @Override
    public Layer obtainLayer(Geometry geoJson, String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (geoJson == null) {
            throw new WebApplicationException("GeoJson boundary must be specified for layer " + layerId,
                HttpStatus.BAD_REQUEST);
        }
        List<Feature> hotProjectLayers = kcApiClient.getCollectionItemsByGeometry(geoJson, HOT_PROJECTS);
        return fromHotProjectLayers(hotProjectLayers);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return HOT_LAYER_ID.equals(layerId);
    }

    /**
     * A single layer is constructed from all <b>dto</b> features
     */
    Layer fromHotProjectLayers(List<Feature> dto) {
        if (dto == null || dto.isEmpty()) {
            return null;
        }
        //The entire collection is one layer
        Layer.LayerBuilder builder = Layer.builder()
            .id(HOT_LAYER_ID)
            .source(LayerSource.builder() //source is required to calculate this layers legend
                .type(GEOJSON)
                //enrich features with HOT Project URL
                .data(new FeatureCollection(dto.stream().peek(f -> {
                    if (f.getProperties() != null) {
                        Integer projectId = getFeatureProperty(f, PROJECT_ID, Integer.class);
                        if (projectId != null) {
                            f.getProperties().put(PROJECT_LINK, HOT_PROJECTS_URL + projectId);
                        }
                    }
                }).toArray(Feature[]::new)))
            .build());
        return builder.build();
        //the rest params are set by LayerConfigService
    }
}
