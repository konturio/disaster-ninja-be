package io.kontur.disasterninja.domain;

import lombok.Data;

import java.util.List;

@Data
public class DatasetStats {

    private Double minValue;

    private Double maxValue;

    private Double meanValue;

    private Double stddevValue;

}
