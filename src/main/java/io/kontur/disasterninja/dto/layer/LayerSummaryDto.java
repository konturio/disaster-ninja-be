package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.Data;

import java.util.List;

@Data
public class LayerSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final LayerCategory category;
    private final String group;
    private final boolean boundaryRequiredForRetrieval;
    private final boolean eventIdRequiredForRetrieval;
    private final LegendDto legend;
    private final List<String> copyrights;
    private final boolean ownedByUser;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(layer.getId(), layer.getName(),
            layer.getDescription(), layer.getCategory() == null ? null : layer.getCategory(),
            layer.getGroup(), layer.isBoundaryRequiredForRetrieval(), layer.isEventIdRequiredForRetrieval(),
            LegendDto.fromLegend(layer.getLegend()),
            layer.getCopyrights(), layer.isOwnedByUser());
    }
}
