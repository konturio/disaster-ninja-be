package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LayerService {
    @Autowired
    KcApiClient kcApiClient;
    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    LayerConfigService layerConfigService;
    @Autowired
    List<LayerProvider> providers;

    public List<Layer> getList(Geometry geoJSON) {
        Map<String, Layer> layers = new HashMap<>();

        providers.stream().map(it -> it.obtainLayers(geoJSON))
            .reduce(new ArrayList<>(), (a, b) -> {
                a.addAll(b);
                return a;
            }).forEach(l -> layers.put(l.getId(), l));

        Map<String, Layer> configs = layerConfigService.getConfigs();

        configs.forEach((layerName, config) -> {
            if (!layers.containsKey(layerName)) {
                //only global overlays are added if not received from providers
                if (config.isGlobalOverlay()) {
                    layers.put(layerName, config);
                }
            } else {
                //apply configs to loaded layers
                layers.get(layerName).mergeFrom(config);
            }
        });
        return new ArrayList<>(layers.values());
    }

//    public Layer get(String layerId) {
//        return null; //todo
//    }
}
