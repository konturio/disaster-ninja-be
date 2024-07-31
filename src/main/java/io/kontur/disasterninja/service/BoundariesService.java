package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.LayersApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoundariesService {

    private static final String KONTUR_BOUNDARIES = "konturBoundaries";

    private final LayersApiClient layersApiClient;

    private final GeometryTransformer geometryTransformer;

    public FeatureCollection getBoundaries(GeoJSON geoJSON) {
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(geoJSON);
        Point point = geometryTransformer.getPointFromGeometry(geometry);
        List<Feature> features = layersApiClient.getCollectionFeatures(point, KONTUR_BOUNDARIES, null);
        return new FeatureCollection(features.toArray(Feature[]::new));
    }

}
