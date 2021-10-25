package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.domain.enums.LayerStepShape;
import lombok.Data;

import java.util.Map;

@Data
public class LegendStepDto {
    private final String paramName;
    private final String paramValue;
    private final String stepName;
    private final String stepShape;
    private final Map<String, String> style; //todo map serialization test (quotes for FE)

    public static LegendStepDto fromLegendStep(LegendStep legendStep) {
        return legendStep == null ? null : new LegendStepDto(legendStep.getParamName(), legendStep.getParamValue(),
            legendStep.getStepName(), legendStep.getStepShape() == null ? null : legendStep.getStepShape().toString(),
            legendStep.getStyle());
    }

    public LegendStep toLegendStep() {
        return new LegendStep(paramName, paramValue, stepName, LayerStepShape.fromString(stepShape), style);
    }

}
