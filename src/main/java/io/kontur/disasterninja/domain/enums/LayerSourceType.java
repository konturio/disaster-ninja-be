package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LayerSourceType {
    VECTOR("vector"),
    RASTER("raster"),
    GEOJSON("geojson");

    private final String value;

    LayerSourceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LayerSourceType fromString(String value) {
        return Arrays.stream(LayerSourceType.values())
                .filter(t -> t.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with value " + value));
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
