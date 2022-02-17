package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerDetailsSearchDto {
    private final GeoJSON geoJSON;
    private final Map<String, Boolean> shouldApplyGeometryFilterPerLayerId;
    private final UUID eventId; //event id
}
