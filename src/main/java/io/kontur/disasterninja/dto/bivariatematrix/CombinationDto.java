package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.COLOR_COMMENT;

@Data
@JsonInclude
public class CombinationDto {

    private String color;
    private List<String> corner;
    @JsonProperty(COLOR_COMMENT)
    private String colorComment;
}
