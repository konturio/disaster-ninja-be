package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LayerService {
    private static final Logger LOG = LoggerFactory.getLogger(LayerService.class);
    final LayerConfigService layerConfigService;
    final List<LayerProvider> providers;
    final EventApiService eventApiService;

    private final GeometryTransformer geometryTransformer;

    public List<Layer> getList(GeoJSON geoJSON, UUID eventId) {
        Map<String, Layer> layers = new HashMap<>();
        Geometry boundary = geometryTransformer.getGeometryFromGeoJson(geoJSON);

        //load layers from providers
        providers.stream().map(it -> {
                try {
                    return it.obtainLayers(boundary, eventId);
                } catch (Exception e) {
                    LOG.error("Caught exception while obtaining layers from {}: {}", it.getClass().getSimpleName(),
                        e.getMessage(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
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

        return layers.values().stream()
                .sorted(Comparator.comparing(layer -> Optional.ofNullable(layer.getOrderIndex())
                        .orElse(Integer.MAX_VALUE)))
                .toList();
    }

    public List<Layer> get(GeoJSON geoJSON, List<String> layerIds, UUID eventId) {
        Geometry boundary = geometryTransformer.getGeometryFromGeoJson(geoJSON);
        List<Layer> result = new ArrayList<>();

        for (String layerId : layerIds) {
            Layer layer = null;
            for (LayerProvider provider : providers) {
                //find first by layer id and use it
                if (provider.isApplicable(layerId)) {
                    layer = provider.obtainLayer(boundary, layerId, eventId);
                    if (layer != null) {
                        LOG.info("Found layer by id {} by provider {}", layerId, provider.getClass().getSimpleName());
                        layerConfigService.applyConfig(layer);
                        result.add(layer);
                    }
                    break;
                }
            }
            //if not found - use global overlay from config
            if (layer == null) {
                layer = layerConfigService.getGlobalOverlays().get(layerId);
                if (layer != null && layer.getSource() != null) {
                    LOG.info("No loaded layer found by id, using a global overlay: {}", layerId);
                    result.add(layer);
                }
            }
        }

        if (result.isEmpty()) {
            throw new WebApplicationException("Layer not found / no layer data found by id and boundary!",
                    HttpStatus.NOT_FOUND);
        }
        return result;
    }
}
