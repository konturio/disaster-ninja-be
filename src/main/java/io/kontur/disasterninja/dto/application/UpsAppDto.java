package io.kontur.disasterninja.dto.application;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.wololo.geojson.Geometry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.kontur.disasterninja.service.GeometryTransformer.geometriesAreEqual;

@Data
public class UpsAppDto {

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

    /*
     * TODO: can this method be removed? We are not comparing DTOs anywhere.
     *  equals() might be needed because of hashcode(),
     *  but this DTO isn't used in any hash based collection either
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpsAppDto upsAppDto)) return false;
        return isPublic == upsAppDto.isPublic
                && Objects.equals(id, upsAppDto.id)
                && Objects.equals(name, upsAppDto.name)
                && Objects.equals(description, upsAppDto.description)
                && Objects.equals(ownedByUser, upsAppDto.ownedByUser)
                && Objects.equals(featuresConfig, upsAppDto.featuresConfig)
                && geometriesAreEqual(centerGeometry, upsAppDto.centerGeometry)
                && Objects.equals(zoom, upsAppDto.zoom)
                && Objects.equals(sidebarIconUrl, upsAppDto.sidebarIconUrl)
                && Objects.equals(faviconUrl, upsAppDto.faviconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isPublic, ownedByUser, featuresConfig, centerGeometry, zoom,
                sidebarIconUrl, faviconUrl);
    }
}
