package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Data
public class LayerSourceDto {

    private final LayerSourceType type;
    private final List<String> urls;//for 'vector' and 'raster' only
    private final Integer tileSize;//for 'vector' and 'raster' only
    private final GeoJSON data; //for geoJson only

    public static LayerSourceDto fromLayerSource(LayerSource layerSource) {
        if (layerSource == null) {
            return null;
        }
        List<String> urls = layerSource.getUrls();
        return new LayerSourceDto(layerSource.getType(), urls != null ? List.copyOf(urls) : null,
                layerSource.getTileSize(), layerSource.getData());
    }
}
