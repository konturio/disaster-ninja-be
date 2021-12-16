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
    /**
     * nullable, internal (not disclosed to DTO). If specified - it's used in features filtering but
     * paramValue is set to dto (single paramValue value into all paramPattern matches)
     */
    private final String paramPattern;
    private final Object paramValue; //null not allowed
    private final String axis;
    private final Double axisValue;
    private final String stepName;
    private final LayerStepShape stepShape;
    private final Map<String, Object> style;
    private final String sourceLayer;
    @JsonIgnore
    private int order;
}