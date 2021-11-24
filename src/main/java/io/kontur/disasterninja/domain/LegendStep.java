package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kontur.disasterninja.domain.enums.LayerStepShape;
import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;

@Data
@ConstructorBinding
public class LegendStep {
    private final String paramName;
    private final Object paramValue; //null not allowed
    private final String axis;
    private final Double axisValue;
    private String stepName; //might be changed when applying localization
    private final LayerStepShape stepShape;
    private final Map<String, Object> style;
    private final String sourceLayer;
    @JsonIgnore
    private int order;

    public LegendStep(String paramName, Object paramValue, String axis, Double axisValue, String stepName,
                      LayerStepShape stepShape, Map<String, Object> style, String sourceLayer) {
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.axis = axis;
        this.axisValue = axisValue;
        this.stepName = stepName;
        this.stepShape = stepShape;
        this.style = style;
        this.sourceLayer = sourceLayer;
    }
}