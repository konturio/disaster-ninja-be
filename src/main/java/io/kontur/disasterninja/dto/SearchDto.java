package io.kontur.disasterninja.dto;

import lombok.Data;
import org.wololo.geojson.FeatureCollection;

@Data
public class SearchDto {
    private FeatureCollection locations;
}
