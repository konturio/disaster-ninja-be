package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.Data;

import java.util.List;

@Data
public class Legend {
    private final String name;
    private final LegendType type;
    private final List<LegendStep> steps;
}
