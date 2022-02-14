package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

@Component
public class LayersApiClient extends RestClientWithBearerAuth {
    private static final Logger LOG = LoggerFactory.getLogger(LayersApiClient.class);

    private static final String COLLECTIONS_URI = "/collections";
    private static final String LAYERS_SEARCH_URI = COLLECTIONS_URI + "/search";
    private static final String LAYERS_FEATURES_SEARCH_URI = COLLECTIONS_URI + "/%s/items/search";
    private static final String COLLECTION_BY_ID_URL = COLLECTIONS_URI + "/%s";

    private final RestTemplate layersApiRestTemplate;
    @Value("${kontur.platform.layersApi.pageSize}")
    private int pageSize;

    public LayersApiClient(RestTemplate layersApiRestTemplate,
                           KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.layersApiRestTemplate = layersApiRestTemplate;
    }

    public Collection createCollection(LayerCreateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(COLLECTIONS_URI, HttpMethod.POST, new HttpEntity<>(dto, httpHeadersWithBearerAuth()),
                new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public Collection updateCollection(String id, LayerUpdateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.PUT,
                new HttpEntity<>(dto, httpHeadersWithBearerAuth()), new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public void deleteCollection(String id) {
        layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.DELETE,
                new HttpEntity<>(null, httpHeadersWithBearerAuth()), new ParameterizedTypeReference<>() {});
    }

    public List<Collection> getCollections(Geometry geoJson) {
        List<Collection> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiCollections> response = layersApiRestTemplate
                    .exchange(LAYERS_SEARCH_URI, HttpMethod.POST, new HttpEntity<>(body, httpHeadersWithBearerAuth()),
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

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiFeatureCollection> response = layersApiRestTemplate
                    .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, collectionId), HttpMethod.POST,
                            new HttpEntity<>(body, httpHeadersWithBearerAuth()),
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
                .exchange(String.format(COLLECTION_BY_ID_URL, collectionId), HttpMethod.GET, new HttpEntity<>(null, null),
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
