package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.enums.LayerStepShape;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Builder
@Jacksonized
@RequiredArgsConstructor
@AllArgsConstructor
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
    private final String stepIconFill;
    private final String stepIconStroke;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int order;
}