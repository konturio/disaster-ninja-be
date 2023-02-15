package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsStatisticsConfigurationDto {

    private String formula;

    private String x;

    private String y;

}
