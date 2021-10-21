package io.kontur.disasterninja.dto.layer;

import lombok.Data;
import org.wololo.geojson.Geometry;

@Data
public class LayerSummaryInputDto {
    private final String id; //event id
    private final Geometry geoJSON;
}
