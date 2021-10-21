package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.ArrayList;
import java.util.List;

@Service
public class LayerService {
    @Autowired
    KcApiClient kcApiClient;
    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    List<LayerProvider> providers;

    public List<Layer> getList(Geometry geoJSON) {
        return providers.stream().map(it -> it.obtainLayers(geoJSON))
            .reduce(new ArrayList<>(), (a, b) -> {
                a.addAll(b);
                return a;
            });
    }

    public Layer get(String layerId) {
        return null; //todo
    }
}
