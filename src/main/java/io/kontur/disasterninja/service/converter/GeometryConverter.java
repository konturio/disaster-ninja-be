package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONReader;

@Component
public class GeometryConverter {

    private static final Logger LOG = LoggerFactory.getLogger(GeometryConverter.class);

    private static final GeoJSONReader reader = new GeoJSONReader();
    private static final PreparedGeometryFactory preparedGeometryFactory = new PreparedGeometryFactory();

    public static PreparedGeometry getPreparedGeometryFromRequest(Geometry geoJson) {
        try {
            org.locationtech.jts.geom.Geometry geometry = getJtsGeometryFromRequest(geoJson);
            return preparedGeometryFactory.create(geometry);
        } catch (Exception e) {
            LOG.warn("Caught exception while building Prepared geometry from geojson: {}", e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static org.locationtech.jts.geom.Geometry getJtsGeometryFromRequest(Geometry geoJson) {
        try {
            return getJtsGeometry(geoJson);
        } catch (Exception e) {
            LOG.warn("Caught exception while building JTS geometry from geojson: {}", e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static org.locationtech.jts.geom.Geometry getJtsGeometry(Geometry geoJson) {
        if (geoJson == null) {
            return null;
        }
        return reader.read(geoJson);
    }
}
