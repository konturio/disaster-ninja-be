package io.kontur.disasterninja.dto;

import lombok.Data;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.kontur.disasterninja.service.GeometryTransformer.geometriesAreEqual;

@Data
public class AppDto {
    private UUID id;
    private String name;
    private String description;
    private boolean isPublic;
    private Boolean ownedByUser;
    private List<String> features;
    private Geometry centerGeometry;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppDto appDto)) return false;
        return isPublic == appDto.isPublic
            && Objects.equals(id, appDto.id)
            && Objects.equals(name, appDto.name)
            && Objects.equals(description, appDto.description)
            && Objects.equals(ownedByUser, appDto.ownedByUser)
            && Objects.equals(features, appDto.features)
            && geometriesAreEqual(centerGeometry, appDto.centerGeometry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isPublic, ownedByUser, features, centerGeometry);
    }
}