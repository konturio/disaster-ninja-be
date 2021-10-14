package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LegendItem;
import io.kontur.disasterninja.domain.enums.LegendItemType;
import lombok.Data;

@Data
public class LegendItemDto {
    private final String type;
    private final String paramName;
    private final String value;
    private final String icon;
    private final String name;
    private final String fillColor;
    private final String lineColor;
    private final String outlineColor;

    public static LegendItemDto fromLegendItem(LegendItem legendItem) {
        return new LegendItemDto(legendItem.getType().toString(), legendItem.getParamName(), legendItem.getValue(),
            legendItem.getIcon(), legendItem.getName(), legendItem.getFillColor(), legendItem.getLineColor(),
            legendItem.getOutlineColor());
    }

    public LegendItem toLegendItem() {
        return new LegendItem(LegendItemType.fromString(type), paramName, value, icon, name, fillColor, lineColor, outlineColor);
    }

}
