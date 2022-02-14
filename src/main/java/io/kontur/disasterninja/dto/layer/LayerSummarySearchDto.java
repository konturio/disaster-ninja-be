package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerSummarySearchDto {
    private final UUID id; //event id
    private final GeoJSON geoJSON;
}
