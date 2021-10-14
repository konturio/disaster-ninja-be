package io.kontur.disasterninja.client;

import io.kontur.disasterninja.domain.Layer;
import org.wololo.geojson.GeoJSON;

import java.util.List;

public interface LayerProvider {
    List<Layer> obtainLayers(GeoJSON geoJSON);
    Layer obtainLayer(String layerId);
    Boolean isApplicable(String layedId); //todo applicable for what? why boxed boolean?
}
