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

    /**
     * Return list of layers available from this LayerProvider. Layer.Source is not populated for layers returned!
     *
     * @param geoJSON if specified - used to filter features by intersection
     * @param eventId used by EventShapeLayerProvider
     * @return list of layers available from this LayerProvider.
     */
    List<Layer> obtainLayers(Geometry geoJSON, UUID eventId);

    Layer obtainLayer(String layerId, UUID eventId);

    boolean isApplicable(String layerId);
}
