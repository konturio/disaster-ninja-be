package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.util.JsonUtil;

/**
 * @param maxZoom for 'vector' and 'raster' only (see source.type)
 * @param minZoom for 'vector' and 'raster' only (see source.type)
 */
public record LayerDetailsDto(String id, Integer maxZoom, Integer minZoom, LayerSourceDto source, Legend legend,
                              String style, String popupConfig, boolean ownedByUser) {

    public static LayerDetailsDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerDetailsDto(layer.getId(), layer.getMaxZoom(), layer.getMinZoom(),
                LayerSourceDto.fromLayer(layer), layer.getLegend(),
                layer.getMapStyle() != null ? JsonUtil.writeJson(layer.getMapStyle()) : null,
                layer.getPopupConfig() != null ? JsonUtil.writeJson(layer.getPopupConfig()) : null, layer.isOwnedByUser());
    }
}
