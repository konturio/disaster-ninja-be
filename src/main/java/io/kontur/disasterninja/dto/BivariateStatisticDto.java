package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BivariateStatisticDto {

    private List<BivariateLayerLegendQuery.Overlay> overlays;

    private List<BivariateLayerLegendQuery.Indicator> indicators;
}
