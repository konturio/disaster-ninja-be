package io.kontur.disasterninja.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Data
@Getter
@Setter
public class AdvancedAnalyticsRequestDto {
    private List<AdvancedAnalyticsRequestValuesDto> values;
    private GeoJSON features;
}
