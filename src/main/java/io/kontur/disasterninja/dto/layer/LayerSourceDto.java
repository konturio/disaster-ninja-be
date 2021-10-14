package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

@Data
public class LayerSourceDto {
    private final LayerSourceType type;
    private final String url;
    private final Double tileSize;
    private final GeoJSON data;

    public static LayerSourceDto fromLayerSource(LayerSource layerSource) {
        return new LayerSourceDto(layerSource.getType(), layerSource.getUrl(),
            layerSource.getTileSize(), layerSource.getData());
    }
}
