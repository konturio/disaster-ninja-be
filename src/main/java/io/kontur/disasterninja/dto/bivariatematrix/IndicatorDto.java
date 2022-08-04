package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude
public class IndicatorDto {

    private String name;
    private String label;
    private List<String> copyrights;
    private List<List<String>> direction;

}
