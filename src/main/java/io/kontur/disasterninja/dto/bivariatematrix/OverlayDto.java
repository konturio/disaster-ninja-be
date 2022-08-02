package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescriptionForOverlay;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude
public class OverlayDto {

    private String name;
    private String description;
    private BivariateLegendAxisDescriptionForOverlay x;
    private BivariateLegendAxisDescriptionForOverlay y;
    private List<ColorDto> colors;
    private Integer order;
}
