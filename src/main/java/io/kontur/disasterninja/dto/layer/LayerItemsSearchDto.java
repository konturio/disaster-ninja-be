package io.kontur.disasterninja.dto.layer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerItemsSearchDto {
    @Schema(example = "58851b50-9574-4aec-a3a6-425fa18dcb54")
    private final UUID appId;
    private final GeoJSON geoJSON;
}
