package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LayerService {
    private static final Logger LOG = LoggerFactory.getLogger(LayerService.class);
    final KcApiClient kcApiClient;
    final InsightsApiClient insightsApiClient;
    final LayerConfigService layerConfigService;
    final List<LayerProvider> providers;
    final EventApiService eventApiService;

    private final GeometryTransformer geometryTransformer;

    public List<Layer> getList(GeoJSON geoJSON, UUID eventId) {
        Map<String, Layer> layers = new HashMap<>();
        Geometry boundary = geometryTransformer.getGeometryFromGeoJson(geoJSON);

        //load layers from providers
        providers.stream().map(it -> it.obtainLayers(boundary, eventId))
            .reduce(new ArrayList<>(), (a, b) -> {
                a.addAll(b);
                return a;
            }).forEach(l -> layers.put(l.getId(), l)); //if there are multiple layers with same id - just one of them will be kept

        //apply layer configs
        layers.values().forEach(layerConfigService::applyConfig);

        //add global overlays
        layerConfigService.getGlobalOverlays().forEach((id, config) -> {
            if (!layers.containsKey(id)) { //can be already loaded by a provider
                layers.put(id, config);
            }
        });

        return new ArrayList<>(layers.values());
    }

    public Layer get(String layerId, UUID eventId) {
        Layer result;
        //find first and return
        for (LayerProvider provider : providers) {
            result = provider.obtainLayer(layerId, eventId);
            if (result != null) {
                LOG.info("Found layer by id {} by provider {}", layerId, provider.getClass().getSimpleName());
                layerConfigService.applyConfig(result);
                return result;
            }
        }

        //return global overlay
        result = layerConfigService.getGlobalOverlays().get(layerId);
        if (result != null) {
            LOG.info("No loaded layer found by id, returning a global overlay: {}", layerId);
        }
        return result; //todo test it should return 404 if not found #7385
    }
}
