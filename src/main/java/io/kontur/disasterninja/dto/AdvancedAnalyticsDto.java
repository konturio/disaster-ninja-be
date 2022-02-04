package io.kontur.disasterninja.dto;

import lombok.*;

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
