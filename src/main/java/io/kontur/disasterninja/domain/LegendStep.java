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
    private final String paramValue;
    private final String axis;
    private final Double axisValue;
    private final String stepName;
    private final LayerStepShape stepShape;
    private final Map<String, String> style;
    @JsonIgnore
    private int order;
}