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

import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;

@Service
public class HotLayerProvider implements LayerProvider {

    public static final String HOT_ID = "hotProjects";

    @Autowired
    KcApiClient kcApiClient;

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON) {
        List<Feature> hotProjectLayers = kcApiClient.getHotProjectLayer(geoJSON);
        return List.of(fromHotProjectLayers(hotProjectLayers));
    }

    @Override
    public Layer obtainLayer(String layerId) {
        return null; //todo
    }

    @Override
    public Boolean isApplicable(String layerId) {
        return HOT_ID.equals(layerId);
    }

    Layer fromHotProjectLayers(List<Feature> dto) {
        if (dto == null) {
            return null;
        }
        //The entire collection is one layer
        Layer result = Layer.builder().id(HOT_ID)
            .build();
        result.setSource(LayerSource.builder()
            .type(GEOJSON)
            .data(new FeatureCollection(dto.toArray(new Feature[0])))
            .build());
        return result;
        //todo set anything else?
    }
}
