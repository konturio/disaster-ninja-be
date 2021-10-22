package io.kontur.disasterninja.service.layers;


import io.kontur.disasterninja.domain.Layer;

public interface LayerConfigService {
    void applyConfig(Layer input);
}
