package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;

@Service
public class HotLayerProvider implements LayerProvider {

    @Autowired
    KcApiClient kcApiClient;

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        List<Feature> hotProjectLayers = kcApiClient.getHotProjectLayer(geoJSON);
        Layer layer = fromHotProjectLayers(hotProjectLayers);
        return layer == null ? List.of() : List.of(layer);
    }

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
        //todo set anything else?
    }
}
