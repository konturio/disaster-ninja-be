package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;

public record LayerDto(String id, LayerSummaryDto summary, LayerDetailsDto details) {

    public static LayerDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerDto(layer.getId(), LayerSummaryDto.fromLayer(layer),
                LayerDetailsDto.fromLayer(layer));
    }
}
