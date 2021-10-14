package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LegendItemType;
import lombok.Data;

@Data
public class LegendItem {
    private final LegendItemType type;
    private final String paramName;
    private final String value;
    private final String icon;
    private final String name;
    private final String fillColor;
    private final String lineColor;
    private final String outlineColor;
}
