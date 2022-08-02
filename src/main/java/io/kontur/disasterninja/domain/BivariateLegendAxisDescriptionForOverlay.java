package io.kontur.disasterninja.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class BivariateLegendAxisDescriptionForOverlay extends BivariateLegendAxisDescription {
    private List<BivariateLegendQuotient> quotients;

    public BivariateLegendAxisDescriptionForOverlay() {

    }

    public BivariateLegendAxisDescriptionForOverlay(String label,
                                                    List<BivariateLegendAxisStep> steps,
                                                    Double quality,
                                                    List<String> quotient,
                                                    List<BivariateLegendQuotient> quotients,
                                                    List<String> parent) {
        super(label, steps, quality, quotient, parent);
        this.quotients = quotients;
    }
}
