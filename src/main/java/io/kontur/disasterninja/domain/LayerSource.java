package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

@Data
@Builder
public class LayerSource {
    private final LayerSourceType type;
    private final String url;
    private final Double tileSize;
    private final FeatureCollection data;
}
