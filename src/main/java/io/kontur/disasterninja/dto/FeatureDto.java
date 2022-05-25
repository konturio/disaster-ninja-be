package io.kontur.disasterninja.dto;

import lombok.Data;

@Data
public class FeatureDto {

    private final String name;
    private final String description;
    private final FeatureType type;

    public enum FeatureType {
        UI_PANEL,
        LAYER,
        EVENT_FEED,
        BIVARIATE_LAYER,
        BIVARIATE_PRESET
    }
}
