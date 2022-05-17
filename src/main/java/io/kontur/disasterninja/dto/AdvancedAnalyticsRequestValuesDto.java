package io.kontur.disasterninja.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdvancedAnalyticsRequestValuesDto {
    private String numerator;
    private String denominator;
    private List<String> calculations;
}
