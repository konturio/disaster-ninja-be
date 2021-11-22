package io.kontur.disasterninja.service.communities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.CommunityResource;
import io.kontur.disasterninja.dto.communities.CommunityDto;
import io.kontur.disasterninja.dto.communities.CommunityType;
import io.kontur.disasterninja.service.GeometryTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.client.KcApiClient.OSM_COMMUNITIES;
import static io.kontur.disasterninja.dto.communities.CommunityType.GLOBAL;
import static io.kontur.disasterninja.dto.communities.CommunityType.LOCAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunitiesService {

    private final KcApiClient kcApiClient;
    private final GeometryTransformer geometryTransformer;
    private final ObjectMapper objectMapper;

    public List<CommunityDto> getCommunitiesByGeometry(GeoJSON geoJSON) {
        Geometry boundary = geometryTransformer.getGeometryFromGeoJson(geoJSON);
        return kcApiClient.getCollectionItemsByGeometry(boundary, OSM_COMMUNITIES)
            .stream()

            .flatMap(t -> toDtos(t).stream()).collect(Collectors.toList());
    }


    private List<CommunityDto> toDtos(Feature feature) {
        CommunityType type;
        if (feature == null || feature.getProperties() == null || feature.getProperties().get("resources") == null) {
            return List.of();
        }
        if (feature.getProperties() != null &&
            Objects.equals("001", feature.getProperties().get("m49"))) {
            //m49 code '001' = Earth
            type = GLOBAL;
        } else {
            type = LOCAL;
        }

        Object resources = feature.getProperties().get("resources");

        if (!(resources instanceof Map)) {
            return List.of();
        }
        List<CommunityResource> communityResources;
        try {
            String str = objectMapper.writeValueAsString(((Map<?, ?>) resources).values());
            communityResources = List.of(objectMapper.createParser(str).readValuesAs(CommunityResource[].class).next()); //todo 1 npe check
        } catch (IOException e) {
            log.warn("Can't read community resources {}: {}", feature.getId(), e.getMessage(), e);
            return List.of();
        }

        return communityResources.stream().map(r -> {
            String name = null;
            String url = null;
            if (r.getStrings() != null) {
                name = (String) r.getStrings().get("name");
            }
            if (name == null && r.getResolved() != null) {
                name = (String) r.getResolved().get("name");
            }
            if (r.getResolved() != null) {
                url = (String) r.getResolved().get("url");
            }

            return new CommunityDto(name, type, null, url); //todo 1 icon url //todo 1 see .svg icons in live-dashboard
        }).collect(Collectors.toList());
    }

}
