package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.kontur.disasterninja.controller.validation.ValidBbox;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    private UserDto user;
    private List<FeatureDto> features;
}
