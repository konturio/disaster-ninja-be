package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import lombok.Data;

@Data
public class LayerDetailsDto {

    private final String id;
    private final Integer maxZoom;
    private final Integer minZoom;
    private final LayerSourceDto source;

    public static LayerDetailsDto fromLayer(Layer layer) {
        return new LayerDetailsDto(layer.getId(), layer.getMaxZoom(), layer.getMinZoom(),
            LayerSourceDto.fromLayerSource(layer.getSource()));
    }
}
