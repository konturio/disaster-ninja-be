package io.kontur.disasterninja.dto.bivariatestatistic;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import lombok.Data;

@Data
public class CorrelationRateDto {

    private BivariateLegendAxisDescription x;
    private BivariateLegendAxisDescription y;
    private Double rate;
    private Double quality;
    private Double correlation;
    private Double avgCorrelationX;
    private Double avgCorrelationY;
}
