package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude
public class MetaDto {
    private Integer max_zoom;
    private Integer min_zoom;
}
