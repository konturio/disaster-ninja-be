package io.kontur.disasterninja.domain;

import lombok.Data;

@Data
public class LegendStep {
    private final String paramName;
    private final String value;
    private final String icon;
    private final String name;
    private final String style;
}