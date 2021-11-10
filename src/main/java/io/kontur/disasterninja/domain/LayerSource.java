package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

@Data
@Builder
public class LayerSource {
    private final LayerSourceType type;
    private final Double tileSize; //for 'vector' and 'raster' only
    private String url; //for 'vector' and 'raster' only
    private String sourceLayer; //for 'vector' and 'raster' only //layer name within a tile
    private FeatureCollection data; //for geoJson only
}
