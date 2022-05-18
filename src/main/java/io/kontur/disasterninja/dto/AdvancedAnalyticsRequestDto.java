package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedAnalyticsRequestDto {
    private List<AdvancedAnalyticsRequestValuesDto> values;
    private GeoJSON features;
}
