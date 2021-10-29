package io.kontur.disasterninja.service.layers;


import io.kontur.disasterninja.domain.Layer;

import java.util.Map;

public interface LayerConfigService {
    Map<String, Layer> getGlobalOverlays();
    void applyConfig(Layer input);
}
