package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

@Data
public class LayerSourceDto {
    private final LayerSourceType type;
    private final String url;//for 'vector' and 'raster' only
    private final Double tileSize;//for 'vector' and 'raster' only
    private final GeoJSON data; //for geoJson only

    public static LayerSourceDto fromLayerSource(LayerSource layerSource) {
        return layerSource == null ? null : new LayerSourceDto(layerSource.getType(), layerSource.getUrl(),
            layerSource.getTileSize(), layerSource.getData());
    }
}
