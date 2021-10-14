package io.kontur.disasterninja.dto.layer.osm;

import lombok.Data;

@Data
public class OsmLayerLink {
    private final String href;
    private final String rel;
    private final String type;
    private final String title;
}
