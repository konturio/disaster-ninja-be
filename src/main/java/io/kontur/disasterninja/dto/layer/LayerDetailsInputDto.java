package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerDetailsInputDto {
    private final GeoJSON geoJSON;
    private final List<String> layerIds;
    private final UUID eventId; //event id
}
