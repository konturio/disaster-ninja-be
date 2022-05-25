package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LayerCategory {
    BASE("base"),
    OVERLAY("overlay");

    private final String value;

    LayerCategory(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LayerCategory fromString(String value) {
        return Arrays.stream(LayerCategory.values())
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
