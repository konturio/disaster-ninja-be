package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

@Data
public class LayerSource {
    private final LayerSourceType type;
    private final String url;
    private final Double tileSize;
    private final GeoJSON data;
}
