package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.ColorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

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
    private List<ColorDto> colors = null;
    @Builder.Default
    private BivariateLegendAxes axes = null;
    private Tooltip tooltip;
}
