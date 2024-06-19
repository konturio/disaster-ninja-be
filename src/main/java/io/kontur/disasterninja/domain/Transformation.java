package io.kontur.disasterninja.domain;

import lombok.Data;

import java.util.List;

@Data
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
