package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.wololo.geojson.Geometry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.kontur.disasterninja.service.GeometryTransformer.geometriesAreEqual;

@Data
public class AppDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;
    private String name;
    private String description;
    private boolean isPublic;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean ownedByUser;
    private Map<String, JsonNode> featuresConfig;
    private Geometry centerGeometry;
    private BigDecimal zoom;
    private String sidebarIconUrl;
    private String faviconUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppDto appDto)) return false;
        return isPublic == appDto.isPublic
                && Objects.equals(id, appDto.id)
                && Objects.equals(name, appDto.name)
                && Objects.equals(description, appDto.description)
                && Objects.equals(ownedByUser, appDto.ownedByUser)
                && Objects.equals(featuresConfig, appDto.featuresConfig)
                && geometriesAreEqual(centerGeometry, appDto.centerGeometry)
                && Objects.equals(zoom, appDto.zoom)
                && Objects.equals(sidebarIconUrl, appDto.sidebarIconUrl)
                && Objects.equals(faviconUrl, appDto.faviconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isPublic, ownedByUser, featuresConfig, centerGeometry, zoom,
                sidebarIconUrl, faviconUrl);
    }
}
