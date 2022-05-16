package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Getter
@Setter
public class AdvancedAnalyticsRequestDto {
    private List<AdvancedAnalyticsRequestValuesDto> values;
    private GeoJSON features;
}
