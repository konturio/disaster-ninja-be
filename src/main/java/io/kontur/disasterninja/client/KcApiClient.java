package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;
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
    RestTemplate kcApiRestTemplate;
    GeoJSONReader reader = new GeoJSONReader();
    @Value("${kontur.platform.kcApi.pageSize}")
    private int pageSize;

    public KcApiClient(RestTemplate kcApiRestTemplate) {
        this.kcApiRestTemplate = kcApiRestTemplate;
    }

    public List<Feature> getOsmLayers(Geometry geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, OSM_LAYERS);
    }

    public List<Feature> getHotProjectLayer(Geometry geoJSON) {
        return getCollectionItemsByGeometry(geoJSON, HOT_PROJECTS);
    }

    public List<Feature> getCollectionItemsByGeometry(Geometry geoJson, String collectionId) {
        org.locationtech.jts.geom.Geometry geoJsonGeometry = getJtsGeometry(geoJson);

        //1 get items by bbox
        List<Feature> features = getCollectionItemsForBbox(geoJsonGeometry, collectionId);

        //2 filter items by geoJson Geometry
        return features.stream()
            .filter(json -> {
                Geometry featureGeom = json.getGeometry();
                return featureGeom == null || //include items without geometry ("global" ones)
                    geoJsonGeometry.intersects(reader.read(featureGeom));
            })
            .collect(Collectors.toList());
    }

    private List<Feature> getCollectionItemsForBbox(
        org.locationtech.jts.geom.Geometry geometry, String collectionId) {
        Envelope env = geometry.getEnvelopeInternal(); //Z axis not taken into account

        String uri = "/collections/" + collectionId + "/items?bbox={bbox}&limit={limit}&offset={offset}";
        int i = 0;

        String bbox = Stream.of(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY())
            .map(String::valueOf).reduce((a, b) -> a + "," + b).get();
        LOG.info("Getting features of collection {} with bbox {}", collectionId, bbox);
        List<Feature> result = new ArrayList<>();

        while (true) {
            int offset = i++ * pageSize;

            ResponseEntity<KcApiFeatureCollection> response = kcApiRestTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null,
                    null), new ParameterizedTypeReference<>() {
                }, bbox, pageSize, offset);

            KcApiFeatureCollection body = response.getBody();
            if (body == null) {
                LOG.info("Empty response returned for collection {} and bbox {}", collectionId, bbox);
                break;
            }

            if (body.getFeatures() == null) {
                break;
            }

            result.addAll(List.of(body.getFeatures()));
            if (result.size() == body.getNumberMatched()) {
                break;
            }
        }

        LOG.info("{} features loaded for collection {} with bbox {}", result.size(), collectionId, bbox);
        return result;
    }

    private org.locationtech.jts.geom.Geometry getJtsGeometry(Geometry geoJson) {
        return reader.read(geoJson);
    }

    @Getter
    private static class KcApiFeatureCollection extends FeatureCollection {
        private final int numberMatched;
        private final int numberReturned;
        @JsonCreator
        public KcApiFeatureCollection(@JsonProperty("features") Feature[] features,
                                      @JsonProperty("numberReturned") int numberReturned,
                                      @JsonProperty("numberMatched") int numberMatched) {
            super(features);
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }
}