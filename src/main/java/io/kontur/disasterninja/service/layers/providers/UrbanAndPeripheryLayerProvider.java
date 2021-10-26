package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.LayerConfigService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.*;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.NAME;

@Service
public class UrbanAndPeripheryLayerProvider implements LayerProvider {
    private static final Set<String> providedLayers = Set.of(SETTL_PERIPHERY_LAYER_ID, URBAN_CORE_LAYER_ID);

    @Autowired
    InsightsApiClient insightsApiClient;

    @Autowired
    LayerConfigService configService;

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        org.wololo.geojson.FeatureCollection urbanCoreAndSettledPeripheryLayers = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJSON);
        return fromUrbanCodeAndPeripheryLayer(urbanCoreAndSettledPeripheryLayers);
    }

    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        throw new NotImplementedException(); //todo
    }

    @Override
    public boolean isApplicable(String layerId) {
        return providedLayers.contains(layerId);
    }

    List<Layer> fromUrbanCodeAndPeripheryLayer(org.wololo.geojson.FeatureCollection dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(dto.getFeatures()).map(f -> { //todo check - any other fields?
            Layer layer = Layer.builder()
                .id((String) f.getId())
                .name((String) f.getProperties().get(NAME))
                .build();
            return layer;
        }).collect(Collectors.toList());
    }
}
