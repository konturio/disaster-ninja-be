package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kontur.disasterninja.domain.Unit;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude
public class IndicatorDto {

    private String name;
    private String label;
    private String emoji;
    private String layerSpatialRes;
    private String layerTemporalExt;
    private List<String> category;
    private List<String> copyrights;
    private List<List<String>> direction;
    private Unit unit;

}
