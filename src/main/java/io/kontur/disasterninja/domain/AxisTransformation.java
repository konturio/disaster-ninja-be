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
public class AxisTransformation {
    private String transformation;
    private Double min;
    private Double mean;
    private Double skew;
    private Double stddev;
    private Double lowerBound;
    private Double upperBound;
}
