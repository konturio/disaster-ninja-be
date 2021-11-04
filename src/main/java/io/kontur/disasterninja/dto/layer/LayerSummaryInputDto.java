package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.Geometry;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerSummaryInputDto {
    private final UUID id; //event id
    private final Geometry geoJSON;
}
