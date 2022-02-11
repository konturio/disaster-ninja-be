package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.dto.layerapi.Collection;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LayersApiClient {

    private static final String LAYERS_SEARCH_URI = "/collections/search";
    private static final String LAYERS_FEATURES_SEARCH_URI = "/collections/%s/items/search";
    private static final String GET_LAYER_URI = "/collections/%s";

    private final RestTemplate layersApiRestTemplate;
    @Value("${kontur.platform.layersApi.pageSize}")
    private int pageSize;

    public LayersApiClient(RestTemplate layersApiRestTemplate) {
        this.layersApiRestTemplate = layersApiRestTemplate;
    }

    public List<Collection> getCollections(Geometry geoJson) {
        List<Collection> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiCollections> response = layersApiRestTemplate
                    .exchange(LAYERS_SEARCH_URI, HttpMethod.POST, new HttpEntity<>(body, headers),
                            new ParameterizedTypeReference<>() {});

            ApiCollections responseBody = response.getBody();
            if (responseBody == null || CollectionUtils.isEmpty(responseBody.getCollections())) {
                break;
            }

            result.addAll(responseBody.getCollections());
            if (result.size() == responseBody.getNumberMatched()) {
                break;
            }
        }

        return result;
    }

    public List<Feature> getCollectionFeatures(Geometry geoJson, String collectionId) {
        Assert.notNull(collectionId, "Layer ID should not be null");

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiFeatureCollection> response = layersApiRestTemplate
                    .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, collectionId), HttpMethod.POST,
                            new HttpEntity<>(body, headers),
                            new ParameterizedTypeReference<>() {});

            ApiFeatureCollection responseBody = response.getBody();
            if (responseBody == null || CollectionUtils.isEmpty(responseBody.getFeatures())) {
                break;
            }

            result.addAll(responseBody.getFeatures());
            if (result.size() == responseBody.getNumberMatched()) {
                break;
            }
        }

        return result;
    }

    public Collection getCollection(String collectionId) {
        ResponseEntity<Collection> response = layersApiRestTemplate
                .exchange(String.format(GET_LAYER_URI, collectionId), HttpMethod.GET, new HttpEntity<>(null, null),
                        new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    @Getter
    private static class ApiCollections {

        private final List<Collection> collections;
        private final int numberMatched;
        private final int numberReturned;

        @JsonCreator
        public ApiCollections(@JsonProperty("collections") List<Collection> collections,
                              @JsonProperty("numberReturned") int numberReturned,
                              @JsonProperty("numberMatched") int numberMatched) {
            this.collections = collections;
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }

    @Getter
    private static class ApiFeatureCollection {

        private final List<Feature> features;
        private final int numberMatched;
        private final int numberReturned;

        @JsonCreator
        public ApiFeatureCollection(@JsonProperty("features") List<Feature> layers,
                                    @JsonProperty("numberReturned") int numberReturned,
                                    @JsonProperty("numberMatched") int numberMatched) {
            this.features = layers;
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }
}
