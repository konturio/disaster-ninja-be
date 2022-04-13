package io.kontur.disasterninja.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdvancedAnalyticsRequestValuesDto {
    private String numerator;
    private String denominator;
    private List<String> calculations;
}
