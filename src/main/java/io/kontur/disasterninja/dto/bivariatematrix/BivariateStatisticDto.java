package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude
public class BivariateStatisticDto {

    private List<BivariateLegendAxisDescription> axis;
    private MetaDto meta;
    private List<IndicatorDto> indicators;
    private List<OverlayDto> overlays;
    private List<CorrelationRateDto> correlationRates;
    private ColorsDto colors;
}
