package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequestDto {

    private UUID appId;

    private GeoJSON features;
}
