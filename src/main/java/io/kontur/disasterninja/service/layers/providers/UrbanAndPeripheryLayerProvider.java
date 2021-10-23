package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.LayerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.NAME;

@Service
public class UrbanAndPeripheryLayerProvider implements LayerProvider {

    @Autowired
    InsightsApiClient insightsApiClient;

    @Autowired
    LayerConfigService configService;

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON) {
        org.wololo.geojson.FeatureCollection urbanCoreAndSettledPeripheryLayers = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJSON);
        return fromUrbanCodeAndPeripheryLayer(urbanCoreAndSettledPeripheryLayers);
    }

    @Override
    public Layer obtainLayer(String layerId) {
        return null; //todo
    }

    @Override
    public Boolean isApplicable(String layerId) {
        return null; //todo
    }

    List<Layer> fromUrbanCodeAndPeripheryLayer(org.wololo.geojson.FeatureCollection dto) {
        if (dto == null) {
            return null;
        }
        return Arrays.stream(dto.getFeatures()).map(f -> { //todo check - any other fields?
            Layer layer = new Layer((String) f.getId());
            layer.setName((String) f.getProperties().get(NAME));
            return layer;
        }).collect(Collectors.toList());
    }
}
