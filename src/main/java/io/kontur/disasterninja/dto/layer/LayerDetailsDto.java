package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.util.JsonUtil;

/**
 * @param maxZoom for 'vector' and 'raster' only (see source.type)
 * @param minZoom for 'vector' and 'raster' only (see source.type)
 */
public record LayerDetailsDto(String id, Integer maxZoom, Integer minZoom, LayerSourceDto source, Legend legend,
                              ObjectNode style, String popupConfig, boolean ownedByUser) {

    public static LayerDetailsDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerDetailsDto(layer.getId(), layer.getMaxZoom(), layer.getMinZoom(),
                LayerSourceDto.fromLayer(layer), layer.getLegend(), layer.getMapStyle(),
                layer.getPopupConfig() != null ? JsonUtil.writeJson(layer.getPopupConfig()) : null, // TODO: get rid of PopupConfig, noone uses it for ages
                layer.isOwnedByUser());
    }
}
