package io.kontur.disasterninja.domain;

import lombok.Data;
import org.wololo.geojson.FeatureCollection;

@Data
public class SearchGroup {
    private String type;
    private FeatureCollection features;
}
