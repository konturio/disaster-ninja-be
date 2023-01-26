package io.kontur.disasterninja.dto.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class LayersApiAppUpdateDto {

    private final boolean showAllPublicLayers;
    @JsonProperty("isPublic")
    private final boolean isPublic;
    private final List<AppLayerUpdateDto> layers;

    public LayersApiAppUpdateDto(boolean showAllPublicLayers, boolean isPublic,
                                 List<AppLayerUpdateDto> layers) {
        this.showAllPublicLayers = showAllPublicLayers;
        this.isPublic = isPublic;
        this.layers = layers;
    }
}
