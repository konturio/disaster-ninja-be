package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.LayerPrototypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.List;

@Service
public class HotLayerProvider implements LayerProvider {

    private static final String HOT_ID = "hotProjects";

    @Autowired
    KcApiClient kcApiClient;

    @Autowired
    LayerPrototypeService prototypeService;

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
        return prototypeService.prototypeOrEmpty(HOT_ID);
        //todo set anything else?
    }
}
