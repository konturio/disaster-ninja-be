package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;

@Service
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
        List<Feature> hotProjectLayers = kcApiClient.getHotProjectLayer(geoJSON);
        Layer layer = fromHotProjectLayers(hotProjectLayers, false);
        return layer == null ? List.of() : List.of(layer);
    }

    /**
     * @param layerId equal to HOT_LAYER_ID otherwise null is returned
     * @param eventId ignored
     * @return Entire <b>HOT_LAYER_ID</b> layer, not limited by geometry
     */
    @Override
    public Layer obtainLayer(String layerId, UUID eventId) { //todo should it return null?
        if (!isApplicable(layerId)) {
            return null;
        }
        return fromHotProjectLayers(kcApiClient.getHotProjectLayer(null), true);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return HOT_LAYER_ID.equals(layerId);
    }

    /**
     * A single layer is constructed from all <b>dto</b> features
     */
    Layer fromHotProjectLayers(List<Feature> dto, boolean includeSourceData) {
        if (dto == null) {
            return null;
        }
        //The entire collection is one layer
        Layer.LayerBuilder builder = Layer.builder().id(HOT_LAYER_ID);
        if (includeSourceData) {
            builder.source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(dto.toArray(new Feature[0])))
                .build());
        }
        return builder.build();
        //the rest params are set by LayerConfigService
    }
}
