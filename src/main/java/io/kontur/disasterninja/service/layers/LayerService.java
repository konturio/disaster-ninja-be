package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

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

    public List<Layer> getList(GeoJSON geoJSON, UUID eventId) {
        Map<String, Layer> layers = new HashMap<>();
        Geometry boundary = getGeometryFromGeoJson(geoJSON);

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

    public List<Layer> get(List<String> layerIds, UUID eventId) {
        List<Layer> result = new ArrayList<>();

        for (String layerId : layerIds) {
            Layer layer = null;
            //find first by layer id and use it
            for (LayerProvider provider : providers) {
                layer = provider.obtainLayer(layerId, eventId);
                if (layer != null) {
                    LOG.info("Found layer by id {} by provider {}", layerId, provider.getClass().getSimpleName());
                    layerConfigService.applyConfig(layer);
                    result.add(layer);
                    break;
                }
            }
            //if not found - use global overlay from config
            if (layer == null) {
                layer = layerConfigService.getGlobalOverlays().get(layerId);
                if (layer != null) {
                    LOG.info("No loaded layer found by id, using a global overlay: {}", layerId);
                    result.add(layer);
                }
            }
        }

        if (result.isEmpty()) {
            throw new WebApplicationException("Layer not found by id!", HttpStatus.NOT_FOUND);
        }
        return result;
    }


    /**
     * Finds all nested features, collects their geometries.
     * @param input GeoJSON of any type
     * @return GeometryCollection with all found geometries. Geometry in case <b>input</b> is a Geometry or a Feature
     */
    protected static Geometry getGeometryFromGeoJson(GeoJSON input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Feature) {
            return ((Feature) input).getGeometry();
        }
        if (input instanceof FeatureCollection) {
            int length = ((FeatureCollection) input).getFeatures().length;
            Geometry[] result = new Geometry[length];

            for (int i = 0; i < length; i++) {
                result[i] = ((FeatureCollection) input).getFeatures()[i].getGeometry();
            }
            return new GeometryCollection(result);
        }
        //it's a Geometry otherwise
        return (Geometry) input;
    }
}
