package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import lombok.Data;

import java.util.List;

import static io.kontur.disasterninja.service.layers.providers.LayerProvider.EVENT_SHAPE_LAYER_ID;

@Data
public class LayerSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final String group;
    private final LegendDto legend;
    private final List<String> copyrights;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(shortenEventShapeId(layer.getId()), layer.getName(),
            layer.getDescription(), layer.getCategory() == null ? null : layer.getCategory().toString(),
            layer.getGroup(), LegendDto.fromLegend(layer.getLegend()), layer.getCopyrights());
    }

    private static String shortenEventShapeId(String full) {
        return full == null ? null :
            full.startsWith(EVENT_SHAPE_LAYER_ID + ".") ? EVENT_SHAPE_LAYER_ID :
                full;
    }
}
