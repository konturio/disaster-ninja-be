package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude
public class Transformation {

    private String transformation;

    private Double min;

    private Double mean;

    private Double skew;

    private Double stddev;

    private Double lowerBound;

    private Double upperBound;

    private List<Double> points;

}
