package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import org.wololo.geojson.GeoJSON;

import java.util.List;

public interface LayerProvider { //todo
    List<Layer> obtainLayers(GeoJSON geoJSON);
    Layer obtainLayer(String layerId);
    Boolean isApplicable(String layedId);
}
