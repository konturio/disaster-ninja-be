package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;

import java.util.List;

public interface LayerProvider {
    String HOT_LAYER_ID = "hotProjects";
    String URBAN_CORE_LAYER_ID = "kontur_urban_core";
    String SETTLED_PERIPHERY_LAYER_ID = "kontur_settled_periphery";
    String EVENT_SHAPE_LAYER_ID = "eventShape";

    /**
     * Layer.Source is not populated for layers returned!
     *
     * @return list of layers available from this LayerProvider.
     */
    List<Layer> obtainLayers(LayerSearchParams searchParams);

    Layer obtainLayer(String layerId, LayerSearchParams searchParams);

    boolean isApplicable(String layerId);
}
