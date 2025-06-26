package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LayerStepShape {
    SQUARE("square"),
    CIRCLE("circle"),
    HEX("hex"),
    HEXAGON("hexagon"),
    ICON("icon");

    private final String value;

    LayerStepShape(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LayerStepShape fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(LayerStepShape.values())
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
