package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Legend {
    private String name;
    private LegendType type;
    private List<LegendStep> steps;
}
