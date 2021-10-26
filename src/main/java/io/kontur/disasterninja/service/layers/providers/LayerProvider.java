package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

public interface LayerProvider {
    String HOT_LAYER_ID = "hotProjects";
    String URBAN_CORE_LAYER_ID = "kontur_urban_core";
    String SETTL_PERIPHERY_LAYER_ID = "kontur_settled_periphery";
    String EVENT_SHAPE_LAYER_ID = "eventShape";

    List<Layer> obtainLayers(Geometry geoJSON, UUID eventId); //todo add comments where any of params is not taken into account

    Layer obtainLayer(String layerId, UUID eventId); //todo add comments where any of params is not taken into account

    boolean isApplicable(String layerId);
}
