package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LegendStep;
import lombok.Data;

@Data
public class LegendStepDto {
    private final String paramName;
    private final String value;
    private final String icon;
    private final String name;
    private final String style; //todo map

    public static LegendStepDto fromLegendStep(LegendStep legendStep) {
        return new LegendStepDto(legendStep.getParamName(), legendStep.getValue(), legendStep.getIcon(),
            legendStep.getName(), legendStep.getStyle());
    }

    public LegendStep toLegendStep() {
        return new LegendStep(paramName, value, icon, name, style);
    }

}
