package io.kontur.disasterninja.dto.layer.osm;

import lombok.Data;

@Data
public class OsmAttribution {
    private final boolean required;
    private final String text;
}
