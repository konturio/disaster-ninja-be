package io.kontur.disasterninja.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BivariateLegendAxes {

    private BivariateLegendAxisDescription x;

    private BivariateLegendAxisDescription y;
}
