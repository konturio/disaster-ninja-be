package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Data;
import org.wololo.geojson.Geometry;

@Data
public class LayerSource {
    private final LayerSourceType type;
    private final String url;
    private final Double tileSize;
    private final Geometry data;
}
