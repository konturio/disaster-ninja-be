package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.kontur.disasterninja.domain.enums.LayerStepShape;
import lombok.Data;

import java.util.Map;

@Data
public class LegendStep {
    private final String paramName;
    private final String paramValue;
    private final String stepName;
    private final LayerStepShape stepShape;
    private final Map<String, String> style;
    @JsonIgnore
    private int order;
}