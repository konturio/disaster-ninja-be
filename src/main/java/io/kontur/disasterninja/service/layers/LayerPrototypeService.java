package io.kontur.disasterninja.service.layers;


import io.kontur.disasterninja.domain.Layer;

public interface LayerPrototypeService {
    Layer prototypeOrEmpty(String layerId);
}
