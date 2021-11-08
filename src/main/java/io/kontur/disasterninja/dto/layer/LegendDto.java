package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class LegendDto {
    private final String name;
    private final String type;
    private final List<LegendStepDto> steps;
    private final List<ColorDto> colors;

    public static LegendDto fromLegend(Legend legend) {
        return legend == null ? null : new LegendDto(legend.getName(),
            legend.getType() == null ? null : legend.getType().toString(),
            legend.getSteps().stream().map(LegendStepDto::fromLegendStep).collect(Collectors.toList()),
            legend.getBivariateColors().entrySet().stream().map(it -> new ColorDto(it.getKey(), it.getValue()))
                .collect(Collectors.toList()));
    }

    public Legend toLegend() {
        return new Legend(name, LegendType.fromString(type), steps.stream().map(LegendStepDto::toLegendStep)
            .collect(Collectors.toList()), colors.stream().collect(Collectors
            .toMap(ColorDto::getId, ColorDto::getColor)));
    }
}
