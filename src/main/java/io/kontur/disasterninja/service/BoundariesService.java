package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KcApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Geometry;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoundariesService {

    private static final String BOUNDS = "bounds";

    private final KcApiClient kcApiClient;

    private final GeometryTransformer geometryTransformer;

    public FeatureCollection getBoundaries(String geoJsonString){
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(GeoJSONFactory.create(geoJsonString));
        List<Feature> featureList = kcApiClient.getCollectionItemsByGeometry(geometry, BOUNDS);
        return new FeatureCollection(featureList.toArray(Feature[]::new));
    }

}
