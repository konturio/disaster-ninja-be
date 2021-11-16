package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.domain.enums.LayerStepShape;
import lombok.Data;

import java.util.Map;

@Data
public class LegendStepDto {
    private final String paramName;
    private final String paramValue;
    private final String axis;
    private final Double axisValue;
    private final String stepName;
    private final String stepShape;
    private final Map<String, String> style;
    private final String sourceLayer;

    public static LegendStepDto fromLegendStep(LegendStep legendStep) {
        return legendStep == null ? null : new LegendStepDto(legendStep.getParamName(), legendStep.getParamValue(),
            legendStep.getAxis(), legendStep.getAxisValue(),
            legendStep.getStepName(), legendStep.getStepShape() == null ? null : legendStep.getStepShape().toString(),
            legendStep.getStyle(), legendStep.getSourceLayer());
    }

    public LegendStep toLegendStep() {
        return new LegendStep(paramName, paramValue, axis, axisValue, stepName,
            LayerStepShape.fromString(stepShape), style, sourceLayer);
    }

}
