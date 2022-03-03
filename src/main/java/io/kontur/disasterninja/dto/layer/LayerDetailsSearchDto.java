package io.kontur.disasterninja.dto.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LayerDetailsSearchDto {
    private final GeoJSON geoJSON;
    private final List<String> layersToRetrieveWithGeometryFilter;
    private final List<String> layersToRetrieveWithoutGeometryFilter;
    private final UUID eventId;
    private final String eventFeed;
}
