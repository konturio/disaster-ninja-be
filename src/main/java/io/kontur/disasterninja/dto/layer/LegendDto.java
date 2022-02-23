package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.BivariateLegendAxes;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class LegendDto {
    private final String type;
    private final String linkProperty;
    private final List<LegendStepDto> steps;
    private final List<ColorDto> colors;
    private final BivariateLegendAxes axis;
    private final String name;
    private final List<String> copyrights;
    private final String description;

    public static LegendDto fromLegend(Legend legend) {
        return legend == null ? null : new LegendDto(legend.getType() == null ? null : legend.getType().toString(),
                legend.getLinkProperty(),
                legend.getSteps().stream().map(LegendStepDto::fromLegendStep).collect(Collectors.toList()),
                bivariateColors(legend.getBivariateColors()),
                legend.getBivariateAxes(),
                legend.getName(),
                legend.getCopyrights(),
                legend.getDescription());
    }

    public Legend toLegend() {
        return new Legend(
                LegendType.fromString(type),
                linkProperty,
                steps != null ? steps.stream()
                        .map(LegendStepDto::toLegendStep)
                        .collect(Collectors.toList()) : null,
                colors != null ? colors.stream()
                        .collect(Collectors.toMap(ColorDto::getId, ColorDto::getColor)) : null,
                axis,
                name,
                copyrights,
                description
                );
    }

    private static List<ColorDto> bivariateColors(Map<String, String> input) {
        if (input == null) {
            return null;
        }
        return input.entrySet().stream().map(it -> new ColorDto(it.getKey(), it.getValue()))
                .collect(Collectors.toList());
    }
}
