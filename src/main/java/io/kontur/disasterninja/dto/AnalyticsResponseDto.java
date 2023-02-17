package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.domain.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AnalyticsResponseDto {

    private String formula;

    private String xLabel;

    private String yLabel;

    private Double value;

    private Unit unit;

    private String prefix;
}
