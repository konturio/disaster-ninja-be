package io.kontur.disasterninja.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class BivariateLegendAxes {

    private BivariateLegendAxisDescription x;

    private BivariateLegendAxisDescription y;
}
