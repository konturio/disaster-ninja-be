package io.kontur.disasterninja.dto.layer;

import lombok.Data;
import org.wololo.geojson.Geometry;

import java.util.UUID;

@Data
public class LayerSummaryInputDto {
    private final UUID id; //event id
    private final Geometry geoJSON;
}
