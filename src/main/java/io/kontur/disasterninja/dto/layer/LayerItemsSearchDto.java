package io.kontur.disasterninja.dto.layer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.wololo.geojson.GeoJSON;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LayerItemsSearchDto {
    @Schema(example = "58851b50-9574-4aec-a3a6-425fa18dcb54")
    private UUID appId;
    private GeoJSON geoJSON;
    // In reality the above two fields are mandatory, while the below two are optional
    // But mixing mandatory (final) and optional in the same class didn't work for me
    private Integer limit = null;
    private Integer offset = null;
}
