package io.kontur.disasterninja.dto.layer;

import io.kontur.disasterninja.domain.BivariateLegendAxes;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LegendType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class LegendDto {
    private final LegendType type;
    private final String linkProperty;
    private final List<LegendStepDto> steps;
    private final List<ColorDto> colors;
    private final BivariateLegendAxes axes;

    public static LegendDto fromLegend(Legend legend) {
        return legend == null ? null : new LegendDto(legend.getType(),
            legend.getLinkProperty(),
            legend.getSteps() == null ? null : legend.getSteps().stream().map(LegendStepDto::fromLegendStep)
                .collect(Collectors.toList()),
            bivariateColors(legend.getBivariateColors()),
            legend.getBivariateAxes());
    }

    public Legend toLegend() {
        return new Legend(
            type,
            linkProperty,
            steps != null ? steps.stream()
                .map(LegendStepDto::toLegendStep)
                .collect(Collectors.toList()) : null,
            colors != null ? colors.stream()
                .collect(Collectors.toMap(ColorDto::getId, ColorDto::getColor)) : null,
            axes
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
