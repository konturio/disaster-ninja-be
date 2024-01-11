package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerItemsSearchDto {
    private final UUID appId;
    private final GeoJSON geoJSON;
}
