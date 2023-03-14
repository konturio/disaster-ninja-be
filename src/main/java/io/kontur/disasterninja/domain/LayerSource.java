package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.wololo.geojson.FeatureCollection;

import java.util.List;

@Data
@Builder
@Jacksonized
public class LayerSource {

    private final LayerType type;
    private final Integer tileSize; //for 'vector' and 'raster' only
    private List<String> urls; //for 'vector' and 'raster' only
    private String apiKey;
    private FeatureCollection data; //for geoJson only
}
