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
        Layer layer = fromHotProjectLayers(hotProjectLayers);
        return layer == null ? List.of() : List.of(layer);
    }

    /**
     * @return null, as no data for this layer can be loaded without a GeoJSON boundary
     */
    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        return null; //todo no data can be loaded without a geoJson - too many features #7385
    }

    @Override
    public boolean isApplicable(String layerId) {
        return HOT_LAYER_ID.equals(layerId);
    }

    /**
     * A single layer is constructed from all <b>dto</b> features
     */
    Layer fromHotProjectLayers(List<Feature> dto) {
        if (dto == null) {
            return null;
        }
        //The entire collection is one layer
        return Layer.builder().id(HOT_LAYER_ID)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(dto.toArray(new Feature[0])))
                .build())
            .build();
        //the rest params are set by LayerConfigService
    }
}
