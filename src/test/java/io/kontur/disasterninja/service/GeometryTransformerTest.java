package io.kontur.disasterninja.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(Arrays.stream(fc.getFeatures()).map(Feature::getGeometry).collect(Collectors.toList()),
                Arrays.stream(((GeometryCollection) result).getGeometries()).toList());

        Geometry geometry = new Point(new double[]{1, 0});
        result = geometryTransformer.getGeometryFromGeoJson(geometry);
        assertEquals(geometry, result);

        Feature f = new Feature(new Point(
                new double[]{1, 0}), new HashMap<>());
        result = geometryTransformer.getGeometryFromGeoJson(f);
        assertEquals(f.getGeometry(), result);
    }

    @Test
    public void getPointFromGeometryMoreThanOneElementTest(){
        FeatureCollection fc = new FeatureCollection(new Feature[]{
                new Feature(new Point(new double[]{1, 0}), new HashMap<>()),
                new Feature(new Point(new double[]{2, 0}), new HashMap<>())});
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(fc);
        try {
            geometryTransformer.getPointFromGeometry(geometry);
        } catch (IllegalArgumentException e){
            assertEquals("Geometry contains more than one element", e.getMessage());
        }
    }

    @Test
    public void getPointFromGeometryNotAPointTest(){
        FeatureCollection fc = new FeatureCollection(new Feature[]{
                new Feature(new LineString(new double[][]{{1.1, 2.2}, {3.3, 4.4}}), new HashMap<>())});
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(fc);
        try {
            geometryTransformer.getPointFromGeometry(geometry);
        } catch (IllegalArgumentException e){
            assertEquals("Geometry is not a point", e.getMessage());
        }
    }

    @Test
    public void getPointFromGeometry(){
        FeatureCollection fc = new FeatureCollection(new Feature[]{
                new Feature(new Point(new double[]{1, 0}), new HashMap<>())});
        Geometry geometry = geometryTransformer.getGeometryFromGeoJson(fc);
        Point point = geometryTransformer.getPointFromGeometry(geometry);
        assertEquals("Point", point.getType());

        geometry = new Point(new double[]{1, 0});
        point = geometryTransformer.getPointFromGeometry(geometry);
        assertEquals("Point", point.getType());

        Feature f = new Feature(new Point(
                new double[]{1, 0}), new HashMap<>());
        geometry = geometryTransformer.getGeometryFromGeoJson(fc);
        point = geometryTransformer.getPointFromGeometry(geometry);
        assertEquals("Point", point.getType());
    }
}