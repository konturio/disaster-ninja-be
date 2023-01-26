package io.kontur.disasterninja.dto.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.dto.layerapi.Collection;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class LayersApiAppDto {

    private final UUID id;
    private final boolean showAllPublicLayers;
    private final boolean isPublic;
    private final List<Collection> defaultCollections;

    @JsonCreator
    public LayersApiAppDto(@JsonProperty("id") UUID id,
                           @JsonProperty("showAllPublicLayers") boolean showAllPublicLayers,
                           @JsonProperty("isPublic") boolean isPublic,
                           @JsonProperty("defaultCollections") List<Collection> defaultCollections) {
        this.id = id;
        this.showAllPublicLayers = showAllPublicLayers;
        this.isPublic = isPublic;
        this.defaultCollections = defaultCollections;
    }
}
