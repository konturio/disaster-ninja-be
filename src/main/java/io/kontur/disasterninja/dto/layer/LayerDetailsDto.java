package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import lombok.Data;

@Data
public class LayerDetailsDto {

    private final String id;
    private final Integer maxZoom; //for 'vector' and 'raster' only (see source.type)
    private final Integer minZoom; //for 'vector' and 'raster' only (see source.type)
    private final LayerSourceDto source;
    private final StyleRuleDto legend;
    private final boolean ownedByUser;

    public static LayerDetailsDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerDetailsDto(layer.getId(), layer.getMaxZoom(), layer.getMinZoom(),
            LayerSourceDto.fromLayerSource(layer.getSource()), StyleRuleDto.fromLegend(layer.getLegend()), layer.isOwnedByUser());
    }
}
