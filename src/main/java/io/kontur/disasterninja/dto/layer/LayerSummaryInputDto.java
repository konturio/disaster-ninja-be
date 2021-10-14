package io.kontur.disasterninja.dto.layer;

import lombok.Data;
import org.wololo.geojson.GeoJSON;

@Data
public class LayerSummaryInputDto {
    private final String id; //event id
    private final GeoJSON geoJSON;
}
