package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class FeatureDto {

    private final String name;
    private final String description;
    private final FeatureType type;
    private JsonNode configuration;

    public enum FeatureType {
        UI_PANEL,
        LAYER,
        EVENT_FEED,
        BIVARIATE_LAYER,
        BIVARIATE_PRESET
    }
}
