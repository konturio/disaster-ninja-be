package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
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
    private final Legend legend; //TODO to remove in #9110
    private final List<String> copyrights;
    private final boolean ownedByUser;
    private final ObjectNode featureProperties;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(layer.getId(), layer.getName(),
            layer.getDescription(), layer.getCategory() == null ? null : layer.getCategory(),
            layer.getGroup(), layer.isBoundaryRequiredForRetrieval(), layer.isEventIdRequiredForRetrieval(),
            layer.getLegend(),
            layer.getCopyrights(), layer.isOwnedByUser(), layer.getFeatureProperties());
    }
}
