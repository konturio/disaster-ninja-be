package io.kontur.disasterninja.service;

import org.springframework.stereotype.Service;
import org.wololo.geojson.*;

@Service
public class GeometryTransformer {

    /**
     * Finds all nested features, collects their geometries.
     *
     * @param input GeoJSON of any type
     * @return GeometryCollection with all found geometries. Geometry in case <b>input</b> is a Geometry or a Feature
     */
    public Geometry getGeometryFromGeoJson(GeoJSON input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Feature) {
            return ((Feature) input).getGeometry();
        }
        if (input instanceof FeatureCollection) {
            int length = ((FeatureCollection) input).getFeatures().length;
            Geometry[] result = new Geometry[length];

            for (int i = 0; i < length; i++) {
                result[i] = ((FeatureCollection) input).getFeatures()[i].getGeometry();
            }
            return new GeometryCollection(result);
        }
        //it's a Geometry otherwise
        return (Geometry) input;
    }

    public Point getPointFromGeometry(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            Geometry[] geometries = ((GeometryCollection) geometry).getGeometries();
            if (geometries.length > 1) {
                throw new IllegalArgumentException("Geometry contains more than one element");
            }
            if (!geometries[0].getType().equals("Point")) {
                throw new IllegalArgumentException("Geometry is not a point");
            }
            return (Point) geometries[0];
        }
        if (!(geometry instanceof Point)) {
            throw new IllegalArgumentException("Geometry is not a point");
        }
        return (Point) geometry;
    }
}