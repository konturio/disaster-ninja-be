package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class LayerService {
    private static final Logger LOG = LoggerFactory.getLogger(LayerService.class);
    @Autowired
    KcApiClient kcApiClient;
    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    LayerConfigService layerConfigService;
    @Autowired
    List<LayerProvider> providers;
    @Autowired
    EventApiService eventApiService;
    private Map<String, Layer> configs;

    @PostConstruct
    private void init() {
        configs = layerConfigService.getConfigs();
    }

    public List<Layer> getList(Geometry geoJSON, UUID eventId) {
        Map<String, Layer> layers = new HashMap<>();

        //load layers from providers
        providers.stream().map(it -> it.obtainLayers(geoJSON, eventId))
            .reduce(new ArrayList<>(), (a, b) -> {
                a.addAll(b);
                return a;
            }).forEach(l -> layers.put(l.getId(), l)); //if there are multiple layers with same id - just one of them will be kept

        //apply layer configs
        layerConfigService.applyConfigs(layers);

        return new ArrayList<>(layers.values());
    }

    public Layer get(String layerId, UUID eventId) {
        Map<String, Layer> layers = new HashMap<>();
        //try load from all providers
        providers.forEach(it -> {
            Layer l = it.obtainLayer(layerId, eventId);
            if (l != null) {
                layers.put(it.getClass().getSimpleName(), l);
            }
        });

        //handle errors
        if (layers.size() > 1) {
            LOG.error("More than one layer found by id {}, found by providers: {}",
                layerId, layers.keySet());
            throw new RuntimeException("More than one layer found by id"); //todo test internal server error
        }

        //apply config (if a single layer was found)
        for (Map.Entry<String, Layer> layer : layers.entrySet()) {
            LOG.info("Found layer by id {} by provider {}", layerId, layer.getKey());
            layerConfigService.applyConfig(layer.getValue());
            return layer.getValue();
        }

        //return default config if exists
        if (configs.containsKey(layerId)) {
            LOG.info("No layer found by id, returning default config {}", layerId);
        }
        return configs.get(layerId); //todo test it should return 404 if not found
    }
}
