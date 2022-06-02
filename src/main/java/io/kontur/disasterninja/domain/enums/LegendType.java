package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LegendType {
    BIVARIATE("bivariate"),
    SIMPLE("simple");

    private final String value;

    LegendType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LegendType fromString(String value) {
        return Arrays.stream(LegendType.values())
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
