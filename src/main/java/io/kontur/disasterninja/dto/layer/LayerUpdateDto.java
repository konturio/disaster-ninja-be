package io.kontur.disasterninja.dto.layer;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LayerUpdateDto {

    @Schema(defaultValue = "feature")
    @NotNull
    private final Type itemType = Type.feature;
    private String title;
    private LegendDto legend;

    public enum Type {
        tiles,
        feature
    }
}
