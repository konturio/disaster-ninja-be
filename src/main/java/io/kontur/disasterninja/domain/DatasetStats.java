package io.kontur.disasterninja.domain;

import lombok.Data;

@Data
public class DatasetStats {

    private Double minValue;

    private Double maxValue;

    private Double mean;

    private Double stddev;

}
