package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import org.wololo.geojson.Geometry;

import java.util.List;

public interface LayerProvider {
    List<Layer> obtainLayers(Geometry geoJSON);

    Layer obtainLayer(String layerId);

    Boolean isApplicable(String layerId);
}
