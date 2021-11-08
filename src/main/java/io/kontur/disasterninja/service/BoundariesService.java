package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KcApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoundariesService {

    private static final String BOUNDS = "bounds";

    private final KcApiClient kcApiClient;

    private final GeometryTransformer geometryTransformer;

    public FeatureCollection getBoundaries(String geoJsonString){
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(GeoJSONFactory.create(geoJsonString));
        Point point = geometryTransformer.getPointFromGeometry(geometry);
        List<Feature> featureList = kcApiClient.getCollectionItemsByPoint(point, BOUNDS);
        return new FeatureCollection(featureList.toArray(Feature[]::new));
    }

}
