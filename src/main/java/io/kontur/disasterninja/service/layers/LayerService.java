package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import k2layers.api.model.FeatureGeoJSON;
import k2layers.api.model.GeometryGeoJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LayerService {
    @Autowired
    KcApiClient kcApiClient;
    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    LayerFactory layerFactory;

    public ArrayList<Layer> getList(GeometryGeoJSON geoJSON) {
        ArrayList<Layer> all = new ArrayList<>();
        all.addAll(getOsmLayers(geoJSON));
        all.add(getHotProjectLayer(geoJSON));
        all.addAll(getUrbanCodeAndPeripheryLayers(geoJSON));
        return all;
    }

    private List<Layer> getOsmLayers(GeometryGeoJSON geoJSON) {
        List<FeatureGeoJSON> osmLayers = kcApiClient.getOsmLayers(geoJSON);
        return layerFactory.fromOsmLayers(osmLayers);
    }

    private Layer getHotProjectLayer(GeometryGeoJSON geoJSON) {
        List<FeatureGeoJSON> hotProjectLayers = kcApiClient.getHotProjectLayer(geoJSON);
        return layerFactory.fromHotProjectLayers(hotProjectLayers);
    }

    private List<Layer> getUrbanCodeAndPeripheryLayers(GeometryGeoJSON geoJSON) {
        org.wololo.geojson.FeatureCollection urbanCoreAndSettledPeripheryLayers = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJSON);
        return layerFactory.fromUrbanCodeAndPeripheryLayer(urbanCoreAndSettledPeripheryLayers);
    }
}
