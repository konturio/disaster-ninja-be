package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LayerType {
    VECTOR("vector"),
    RASTER("raster"),
    GEOJSON("geojson"),
    TILES("tiles"),
    FEATURE("feature"),

    // TODO Add new layer types in #15281. Remove types above after they are no longer used by FE
    MAPLIBRE_STYLE_URL("maplibre-style-url");

    private final String value;

    LayerType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LayerType fromString(String value) {
        return Arrays.stream(LayerType.values())
                .filter(t -> t.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
