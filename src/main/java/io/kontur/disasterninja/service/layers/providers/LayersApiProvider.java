package io.kontur.disasterninja.service.layers.providers;

import static io.kontur.disasterninja.service.LayersApiService.LAYER_PREFIX;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.LayersApiService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LayersApiProvider implements LayerProvider {

    private final LayersApiService layersApiService;

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        return layersApiService.findLayers(geoJSON);
    }

    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        return layersApiService.getLayer(geoJSON, layerId, eventId);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
    }

}
