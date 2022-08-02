package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.MAX_ZOOM;
import static io.kontur.disasterninja.domain.DtoFeatureProperties.MIN_ZOOM;

@Data
@JsonInclude
public class MetaDto {
    @JsonProperty(MAX_ZOOM)
    private Integer maxZoom;

    @JsonProperty(MIN_ZOOM)
    private Integer minZoom;
}
