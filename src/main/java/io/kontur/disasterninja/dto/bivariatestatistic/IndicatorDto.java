package io.kontur.disasterninja.dto.bivariatestatistic;

import lombok.Data;

import java.util.List;

@Data
public class IndicatorDto {

    private String name;
    private String label;
    private List<String> copyrights;
    private List<List<String>> direction;

}
