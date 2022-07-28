package io.kontur.disasterninja.dto.bivariatestatistic;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OverlayDto {

    private String name;
    private String description;
    private BivariateLegendAxisDescription x;
    private BivariateLegendAxisDescription y;
    private List<ColorDto> colors;
    private Integer order;
}
