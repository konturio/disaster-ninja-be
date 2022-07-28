package io.kontur.disasterninja.dto.bivariatestatistic;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BivariateStatisticDto {

    private List<OverlayDto> overlays;
    private List<IndicatorDto> indicators;
    private List<BivariateLegendAxisDescription> axis;
    private MetaDto meta;
    private List<CorrelationRateDto> correlationRates;
    private ColorsDto colors;
}
