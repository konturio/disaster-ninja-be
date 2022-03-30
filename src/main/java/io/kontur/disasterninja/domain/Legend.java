package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class Legend {

    private String name;
    private LegendType type;
    private String linkProperty;
    @Builder.Default
    private List<LegendStep> steps = new ArrayList<>();
    @Builder.Default
    private Map<String, String> bivariateColors = null;
    @Builder.Default
    private BivariateLegendAxes bivariateAxes = null;
}
