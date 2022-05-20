package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
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
import org.wololo.geojson.Point;
import org.wololo.jts2geojson.GeoJSONWriter;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.service.converter.GeometryConverter.*;

@Component
public class KcApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(KcApiClient.class);
    public static final String HOT_PROJECTS = "hotProjects";
    public static final String OSM_LAYERS = "osmlayer";
    private final RestTemplate kcApiRestTemplate;
    private final GeoJSONWriter writer = new GeoJSONWriter();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    @Value("${kontur.platform.kcApi.pageSize}")
    private int pageSize;

    public KcApiClient(RestTemplate kcApiRestTemplate) {
        this.kcApiRestTemplate = kcApiRestTemplate;
    }

    public Feature getFeatureFromCollection(Geometry geoJson, String featureId, String collectionId) {
        String uri = "/collections/{collectionId}/items/{featureId}";

        LOG.info("Getting feature by id {} from collection {}", featureId, collectionId);

        ResponseEntity<Feature> response = kcApiRestTemplate
            .exchange(uri, HttpMethod.GET, new HttpEntity<>(null,
                null), new ParameterizedTypeReference<>() {
            }, collectionId, featureId);

        Feature body = response.getBody();
        if (body == null) {
            LOG.info("Empty response returned for collection {} and featureId {}", collectionId, featureId);
            return null;
        }

        if (geoJson != null && body.getGeometry() != null) {
            if (getPreparedGeometryFromRequest(geoJson).intersects(
                getJtsGeometry(body.getGeometry()))) {
                return body;
            } else {
                LOG.info("Feature {} does not intersect with requested boundary {}", body.getId(), geoJson);
                return null;
            }
        }

        return body;
    }

    public List<Feature> getCollectionItemsByGeometry(Geometry geoJson, String collectionId) {
        return getCollectionItems(collectionId, geoJson);
    }

    /**
     * @return List of features whose centroids intersect with <b>geoJson</b>, replacing original features' geometries
     * with their centroids
     */
    public List<Feature> getCollectionItemsByCentroidGeometry(Geometry geoJson, String collectionId) {
        PreparedGeometry geoJsonGeometry = getPreparedGeometryFromRequest(geoJson);
        //1 get items
        List<Feature> features = getCollectionItems(collectionId, geoJson);

        //2 filter items by geoJson Geometry
        return features.stream()
            .map(feature -> {
                Geometry featureGeom = feature.getGeometry();
                //include items without geometry ("global" ones)
                if (featureGeom == null) {
                    return feature;
                }

                //if feature's centroid intersects with requested geoJson - return the feature with centroid's geometry
                Coordinate featureCentroid = new Centroid(getJtsGeometry(featureGeom)).getCentroid();
                org.locationtech.jts.geom.Point centroidPoint = geometryFactory.createPoint(featureCentroid);
                if (geoJsonGeometry.intersects(centroidPoint)) {
                    return new Feature(feature.getId(), writer.write(centroidPoint), feature.getProperties());
                }

                //no intersection
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<Feature> getCollectionItemsByPoint(Point point, String collectionId) {
        String uri = "/collections/" + collectionId + "/itemsByMultipoint?geom={geom}&limit={limit}&offset={offset}";
        int i = 0;

        List<Feature> result = new ArrayList<>();

        while (true) {
            int offset = i++ * pageSize;

            ResponseEntity<KcApiFeatureCollection> response = kcApiRestTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null,
                    null), new ParameterizedTypeReference<>() {
                }, getJtsGeometryFromRequest(point).toString(), pageSize, offset);

            KcApiFeatureCollection body = response.getBody();
            if (body == null) {
                LOG.info("Empty response returned for collection {} and point {}", collectionId, point);
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

        LOG.info("{} features loaded for collection {} with point {}", result.size(), collectionId, point);
        return result;
    }

    protected List<Feature> getCollectionItems(String collectionId,
                                               @NotNull Geometry geometry) {
        String uri = "/collections/" + collectionId + "/itemsByGeometry";

        int i = 0;

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("limit", pageSize);
        body.put("geom", geometry);

        while (true) {
            int offset = i++ * pageSize;
            body.put("offset", offset);

            ResponseEntity<KcApiFeatureCollection> response = kcApiRestTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(body, null),
                        new ParameterizedTypeReference<>() {});

            KcApiFeatureCollection responseBody = response.getBody();
            if (responseBody == null) {
                break;
            }

            if (responseBody.getFeatures() == null) {
                break;
            }

            result.addAll(List.of(responseBody.getFeatures()));
            if (result.size() == responseBody.getNumberMatched()) {
                break;
            }
        }

        return result;
    }

    @Getter
    private static class KcApiFeatureCollection {
        private final Feature[] features;
        private final int numberMatched;
        private final int numberReturned;

        @JsonCreator
        public KcApiFeatureCollection(@JsonProperty("features") Feature[] features,
                                      @JsonProperty("numberReturned") int numberReturned,
                                      @JsonProperty("numberMatched") int numberMatched) {
            this.features = features;
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }
}
