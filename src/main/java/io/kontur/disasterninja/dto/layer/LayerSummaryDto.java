package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import lombok.Data;

@Data
public class LayerSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final String group;
    private final LegendDto legend;
    private final String copyright;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(layer.getId(), layer.getName(), layer.getDescription(),
            layer.getCategory() == null ? null : layer.getCategory().toString(), layer.getGroup(),
            LegendDto.fromLegend(layer.getLegend()), layer.getCopyright());
    }
}
