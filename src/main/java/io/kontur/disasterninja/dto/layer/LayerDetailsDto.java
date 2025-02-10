package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;

/**
 * @param maxZoom for 'vector' and 'raster' only (see source.type)
 * @param minZoom for 'vector' and 'raster' only (see source.type)
 */
public record LayerDetailsDto(String id, Integer maxZoom, Integer minZoom, LayerSourceDto source, Legend legend,
                              String style, String popupConfig, boolean ownedByUser) {

    public static LayerDetailsDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerDetailsDto(layer.getId(), layer.getMaxZoom(), layer.getMinZoom(),
                LayerSourceDto.fromLayer(layer), layer.getLegend(),
                layer.getMapStyle() != null ? layer.getMapStyle().toString() : null,
                layer.getPopupConfig() != null ? layer.getPopupConfig().toString() : null, layer.isOwnedByUser());
    }
}
