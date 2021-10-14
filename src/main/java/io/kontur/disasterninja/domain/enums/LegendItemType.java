package io.kontur.disasterninja.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum LegendItemType {
    BIVARIATE("bivariate"),
    SIMPLE("simple");

    private final String value;

    LegendItemType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LegendItemType fromString(String value) {
        return Arrays.stream(LegendItemType.values())
            .filter(t -> t.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No enum constant with value " + value));
    }

    @Override
    public String toString() {
        return value;
    }
}
