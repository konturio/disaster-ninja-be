package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Legend;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LayerUpdateDto {

    public static final String LAYER_TYPE_TILES = "tiles";
    public static final String LAYER_TYPE_FEATURE = "feature";

    @Schema(defaultValue = "feature")
    @NotNull
    private final String itemType = LAYER_TYPE_FEATURE;
    @JsonAlias("name")
    @Schema(name = "name")
    private String title;
    @JsonAlias("legend")
    @JsonProperty("styleRule")
    @Schema(name = "legend")
    private Legend legend;
    private ObjectNode featureProperties;
    private UUID appId;
}
