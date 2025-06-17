package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.DtoFeatureProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountriesService {

    private static final String KONTUR_BOUNDARIES = "konturBoundaries";

    private final LayersApiClient layersApiClient;
    private final GeometryTransformer geometryTransformer;

    public Set<String> getAffectedCountries(GeoJSON geoJSON) {
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(geoJSON);
        List<Feature> features = layersApiClient.getCollectionFeatures(geometry, KONTUR_BOUNDARIES, null);
        return features.stream()
                .map(f -> (String) f.getProperties().get(DtoFeatureProperties.COUNTRY_CODE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
