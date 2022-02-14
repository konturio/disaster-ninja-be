package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LayerUpdateDto {

    @JsonProperty("itemType")
    @NotNull
    private final Type itemType = Type.feature;
    @JsonProperty("title")
    private String title;
    @JsonProperty("legend")
    private LegendDto legend;

    public enum Type {
        tiles,
        feature
    }
}
