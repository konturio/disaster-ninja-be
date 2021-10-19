package io.kontur.disasterninja.dto.layer;

import k2layers.api.model.GeometryGeoJSON;
import lombok.Data;

@Data
public class LayerSummaryInputDto {
    private final String id; //event id //todo not used
    private final GeometryGeoJSON geoJSON;
}
