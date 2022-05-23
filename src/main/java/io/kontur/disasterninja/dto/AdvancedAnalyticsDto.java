package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdvancedAnalyticsDto {

    private String numerator;

    private String denominator;

    private String numeratorLabel;

    private String denominatorLabel;

    private List<AdvancedAnalyticsValuesDto> analytics;

}
