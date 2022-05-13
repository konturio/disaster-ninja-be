package io.kontur.disasterninja.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.*;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

@Service
public class GeometryTransformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final GeoJSONReader reader = new GeoJSONReader();
    private static final GeoJSONWriter writer = new GeoJSONWriter();

    public Geometry validateAndFixGeometry(Geometry input) {
        org.locationtech.jts.geom.Geometry geometry = reader.read(input);
        if (!geometry.isValid()) {
            return writer.write(GeometryFixer.fix(geometry));
        }
        return input;
    }

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
                throw new WebApplicationException("Geometry contains more than one element", HttpStatus.BAD_REQUEST);
            }
            if (!geometries[0].getType().equals("Point")) {
                throw new WebApplicationException("Geometry is not a point", HttpStatus.BAD_REQUEST);
            }
            return (Point) geometries[0];
        }
        if (!(geometry instanceof Point)) {
            throw new WebApplicationException("Geometry is not a point", HttpStatus.BAD_REQUEST);
        }
        return (Point) geometry;
    }

    public static boolean geometriesAreEqual(org.wololo.geojson.Geometry geometry1,
                                             org.wololo.geojson.Geometry geometry2) {
        return objectMapper.valueToTree(geometry1).equals(objectMapper.valueToTree(geometry2));
    }
}
