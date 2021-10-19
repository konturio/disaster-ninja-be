package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import k2layers.api.model.GeometryGeoJSON;
import lombok.Data;

@Data
public class LayerSource {
    private final LayerSourceType type;
    private final String url;
    private final Double tileSize;
    private final GeometryGeoJSON data;
}
