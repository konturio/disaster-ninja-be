package io.kontur.disasterninja.dto.application;

import com.fasterxml.jackson.databind.JsonNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AppContextDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;
    private String name;
    private String description;
    private boolean isPublic;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean ownedByUser;
    private Map<String, JsonNode> featuresConfig;
    private String sidebarIconUrl;
    private String faviconUrl;

    private boolean showAllPublicLayers;
    private List<Layer> defaultLayers;

    private UserDto user;

    List<EventFeedDto> userFeeds;
}
