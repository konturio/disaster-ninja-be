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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KcApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(KcApiClient.class);
    private static final String HOT_PROJECTS = "hotProjects";
    private static final String OSM_LAYERS = "osmlayer";
    @Autowired
    @Qualifier("kcApiRestTemplate")
    RestTemplate restTemplate;
    @Autowired
    ObjectMapper objectMapper;
    GeoJSONReader reader = new GeoJSONReader();
    @Value("${kontur.platform.kcApi.pageSize}")
    private int pageSize;

    public List<FeatureGeoJSON> getOsmLayers(GeometryGeoJSON geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, OSM_LAYERS);
    }

    public List<FeatureGeoJSON> getHotProjectLayer(GeometryGeoJSON geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, HOT_PROJECTS);
    }

    public List<FeatureGeoJSON> getCollectionItemsByGeometry(GeometryGeoJSON geoJson, String collectionId) {
        Geometry geoJsonGeometry = getGeometry(geoJson);

        //1 get items by bbox
        List<FeatureGeoJSON> featureGeoJsons = getCollectionItemsForBbox(geoJsonGeometry, collectionId);

        //2 filter items by geoJson Geometry
        return featureGeoJsons.stream()
            .filter(json -> {
                Geometry geom = null;
                try {
                    if (json.getGeometry() != null) {
                        geom = reader.read(objectMapper.writeValueAsString(json.getGeometry()));
                    }
                } catch (JsonProcessingException e) {
                    LOG.warn("Can't deserialize geometry from feature json, ignoring: {}", json);
                }
                return geom == null || //include items without geometry ("global" ones)
                    geoJsonGeometry.intersects(geom);
            })
            .collect(Collectors.toList());
    }

    private List<FeatureGeoJSON> getCollectionItemsForBbox(Geometry geometry, String collectionId) {
        Envelope env = geometry.getEnvelopeInternal(); //Z axis not taken into account

        String uri = "/collections/" + collectionId + "/items?bbox={bbox}&limit={limit}&offset={offset}";
        int i = 0;

        Bbox bbox = new Bbox(Stream.of(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY())
            .map(String::valueOf).reduce((a, b) -> a + "," + b).get());
        LOG.info("Getting features of collection {} with bbox {}", collectionId, bbox);
        List<FeatureGeoJSON> result = new ArrayList<>();

        while (true) {
            int offset = i++ * pageSize;

            ResponseEntity<FeatureCollectionGeoJSON> response = restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null,
                    null), new ParameterizedTypeReference<>() {
                }, bbox, pageSize, offset);

            if (response.getBody() == null) {
                LOG.info("Empty response returned for collection {} and bbox {}", collectionId, bbox);
                break;
            }
            if (response.getBody().getFeatures() == null || response.getBody().getFeatures().isEmpty()
                || response.getBody().getFeatures().size() < pageSize) {
                break;
            }

            result.addAll(response.getBody().getFeatures());
        }

        LOG.info("{} features loaded for collection {} with bbox {}", result.size(), collectionId, bbox);
        return result;
    }

    private Geometry getGeometry(GeometryGeoJSON geoJson) {
        try {
            return reader.read(objectMapper.writeValueAsString(geoJson));
        } catch (JsonProcessingException e) {
            LOG.error("Caught exception while serializing geoJson: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
