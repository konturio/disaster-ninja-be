package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerType;

import java.util.*;

public record LayerSummaryDto(String id, String name, LayerType type, String description, LayerCategory category,
                              String group, boolean boundaryRequiredForRetrieval, boolean eventIdRequiredForRetrieval,
                              List<String> copyrights, boolean ownedByUser, ObjectNode featureProperties) {

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(layer.getId(), layer.getName(), layer.getType(),
                layer.getDescription(), layer.getCategory() == null ? null : layer.getCategory(),
                layer.getGroup(), layer.isBoundaryRequiredForRetrieval(), layer.isEventIdRequiredForRetrieval(),
                layer.getCopyrights(), layer.isOwnedByUser(), layer.getFeatureProperties());
    }
}
