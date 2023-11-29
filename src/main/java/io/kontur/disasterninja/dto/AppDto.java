package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.kontur.disasterninja.controller.validation.ValidBbox;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
public class AppDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;
    private String name;
    private String description;
    private boolean isPublic;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean ownedByUser;
    // TODO: remove this field when feature configs from UPS are received within List<FeatureDto> features parameter
    private Map<String, JsonNode> featuresConfig;
    @ValidBbox
    private List<BigDecimal> extent;
    private String sidebarIconUrl;
    private String faviconUrl;
    private JsonNode faviconPack;

    private UserDto user;
    private List<FeatureDto> features;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDto appDto = (AppDto) o;
        return isPublic == appDto.isPublic &&
                Objects.equals(id, appDto.id) &&
                Objects.equals(name, appDto.name) &&
                Objects.equals(description, appDto.description) &&
                Objects.equals(ownedByUser, appDto.ownedByUser) &&
                Objects.equals(featuresConfig, appDto.featuresConfig) &&
                Objects.equals(extent, appDto.extent) &&
                Objects.equals(sidebarIconUrl, appDto.sidebarIconUrl) &&
                Objects.equals(faviconUrl, appDto.faviconUrl) &&
                Objects.equals(user, appDto.user) &&
                Objects.equals(features, appDto.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isPublic, ownedByUser, featuresConfig, extent, sidebarIconUrl, faviconUrl, user, features);
    }
}
