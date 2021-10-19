package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import k2layers.api.model.Bbox;
import k2layers.api.model.FeatureCollectionGeoJSON;
import k2layers.api.model.FeatureGeoJSON;
import k2layers.api.model.GeometryGeoJSON;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KcApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(KcApiClient.class);
    private static final String HOT_PROJECTS = "hotProjects";
    private static final String OSM_LAYERS = "osmlayer";
    @Value("${kontur.platform.kcApi.pageSize}")
    private int pageSize;
    @Autowired
    @Qualifier("kcApiRestTemplate")
    RestTemplate restTemplate;
    @Autowired
    ObjectMapper objectMapper;
    GeoJSONReader reader = new GeoJSONReader();

    public List<FeatureGeoJSON> getOsmLayers(GeometryGeoJSON geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, OSM_LAYERS);
    }

    public List<FeatureGeoJSON> getHotProjectLayer(GeometryGeoJSON geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, HOT_PROJECTS);
    }

    public List<FeatureGeoJSON> getCollectionItemsByGeometry(GeometryGeoJSON geoJson, String collectionId) {
        String uri = "/collections/" + collectionId + "/items?bbox={bbox}&limit={limit}"; //todo pagination/offset

        //////prepare bounding box
        Geometry geoJsonGeometry;
        try {
            geoJsonGeometry = reader.read(objectMapper.writeValueAsString(geoJson));
        } catch (JsonProcessingException e) {
            LOG.error("Caught exception while serializing geoJson: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Envelope env = geoJsonGeometry.getEnvelopeInternal(); //Z axis not taken into account

        ///////1 get items by bbox
        Bbox bbox = new Bbox(Stream.of(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY())
            .map(String::valueOf).reduce((a, b) -> a + "," + b).get());
        LOG.info("Getting features of collection {} with bbox {}", collectionId, bbox);
        ResponseEntity<FeatureCollectionGeoJSON> response = restTemplate
            .exchange(uri, HttpMethod.GET, new HttpEntity<>(null,
                null), new ParameterizedTypeReference<>() {
            }, bbox, pageSize);

        if (response.getBody() == null) {
            LOG.info("Empty response returned for collection {} and bbox {}", collectionId, bbox);
            return null;
        }
        FeatureCollectionGeoJSON itemsByBbox = response.getBody();

        //////2 filter items by geoJsonGeometry
        List<FeatureGeoJSON> features = itemsByBbox.getFeatures().stream()
            .filter(json -> {
                Geometry geom = null;
                try {
                    if (json.getGeometry() != null) {
                        geom = reader.read(objectMapper.writeValueAsString(json.getGeometry()));
                    }
                } catch (JsonProcessingException e) {
                    LOG.warn("Can't deserialize geometry from feature json, ignoring: {}", json);
                }
                return geom == null || geoJsonGeometry.intersects(geom); //also include layers without geometry ("global" ones)
            })
            .collect(Collectors.toList());
        return features;
    }
}
