package io.kontur.disasterninja.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class GeometryTransformerTest {

    @InjectMocks
    private GeometryTransformer geometryTransformer;

    @Test
    public void geojsonGeometryTest() {
        FeatureCollection fc = new FeatureCollection(new Feature[]{
                new Feature(new Point(new double[]{1, 0}), new HashMap<>()),
                new Feature(new Point(new double[]{2, 0}), new HashMap<>())});

        Geometry result = geometryTransformer.getGeometryFromGeoJson(fc);
        Assertions.assertEquals(Arrays.stream(fc.getFeatures()).map(Feature::getGeometry).collect(Collectors.toList()),
                Arrays.stream(((GeometryCollection) result).getGeometries()).toList());

        Geometry geometry = new Point(new double[]{1, 0});
        result = geometryTransformer.getGeometryFromGeoJson(geometry);
        Assertions.assertEquals(geometry, result);

        Feature f = new Feature(new Point(
                new double[]{1, 0}), new HashMap<>());
        result = geometryTransformer.getGeometryFromGeoJson(f);
        Assertions.assertEquals(f.getGeometry(), result);
    }
}