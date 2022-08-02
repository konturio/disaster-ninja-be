package io.kontur.disasterninja.dto.bivariatematrix;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude
public class CombinationDto {

    private String color;
    private List<String> corner;
    @JsonProperty("color_comment")
    private String colorComment;
}
