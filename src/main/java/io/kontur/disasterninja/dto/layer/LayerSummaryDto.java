package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class LayerSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final String group;
    private final List<LegendItemDto> legend;
    private final String copyright;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return new LayerSummaryDto(layer.getId(), layer.getName(), layer.getDescription(), layer.getCategory().toString(),
            layer.getGroup(), layer.getLegend().stream().map(LegendItemDto::fromLegendItem).collect(Collectors.toList()),
            layer.getCopyright());
    }
}
