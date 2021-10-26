package io.kontur.disasterninja.service.layers;


import io.kontur.disasterninja.domain.Layer;

import java.util.Map;

public interface LayerConfigService {
    void applyConfigs(Map<String, Layer> layers);
    void applyConfig(Layer input);
    Map<String, Layer> getConfigs();
}
