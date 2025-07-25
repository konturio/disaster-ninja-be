package io.kontur.disasterninja.util;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.service.converter.GeometryConverter;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.FeatureCollection;

public class CountryBoundaryUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CountryBoundaryUtil.class);

    private CountryBoundaryUtil() {
    }

    public static Geometry loadCountryBoundary(LayersApiClient layersApiClient, String iso3Code) {
        try {
            FeatureCollection fc = layersApiClient.getCountryBoundary(iso3Code);
            if (fc == null || fc.getFeatures() == null || fc.getFeatures().length == 0) {
                return null;
            }
            return GeometryConverter.convertGeometry(fc);
        } catch (Exception e) {
            LOG.error("Failed to load {} boundary: {}", iso3Code.toUpperCase(), e.getMessage(), e);
            return null;
        }
    }
}
