package io.kontur.disasterninja.dto.layer.osm;

import lombok.Data;

import java.util.List;

@Data
public class OsmLayer {
    private final String id;
    private final List<OsmLayerLink> links;
    private final OsmProperties osmProperties;
    private final String type;
}
