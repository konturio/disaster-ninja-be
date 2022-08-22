package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.wololo.geojson.FeatureCollection;

import java.util.List;

@Data
@Builder
@Jacksonized
public class LayerSource {

    private final LayerSourceType type;
    private final Integer tileSize; //for 'vector' and 'raster' only
    private List<String> urls; //for 'vector' and 'raster' only
    private String apiKey;
    private String apiTag;
    private FeatureCollection data; //for geoJson only
}
