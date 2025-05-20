package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.LayersApiApplicationDto;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.*;

import static java.util.Collections.singletonList;

@Component
public class LayersApiClient extends RestClientWithBearerAuth {

    private static final String APPS_URI = "/apps/%s";
    private static final String COLLECTIONS_URI = "/collections";
    private static final String LAYERS_SEARCH_URI = COLLECTIONS_URI + "/search";
    private static final String LAYERS_FEATURES_SEARCH_URI = COLLECTIONS_URI + "/%s/items/search";
    private static final String COLLECTION_BY_ID_URL = COLLECTIONS_URI + "/%s";
    private static final String UPDATE_FEATURES_URL = COLLECTIONS_URI + "/%s/items";

    private final RestTemplate layersApiRestTemplate;
    @Value("${kontur.platform.layersApi.pageSize}")
    private int pageSize;

    public LayersApiClient(RestTemplate layersApiRestTemplate,
                           KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.layersApiRestTemplate = layersApiRestTemplate;
    }

    public List<Collection> getCollections(Geometry geoJson, boolean omitLocalLayers, CollectionOwner collectionOwner,
                                           UUID appId) {
        List<Collection> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        if (collectionOwner != null) {
            body.put("collectionOwner", collectionOwner);
        }
        body.put("geometry", geoJson);
        body.put("omitLocalCollections", omitLocalLayers);
        body.put("limit", pageSize);
        if (appId != null) {
            body.put("appId", appId);
        }

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiCollections> response = layersApiRestTemplate
                    .exchange(LAYERS_SEARCH_URI, HttpMethod.POST,
                            httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                            new ParameterizedTypeReference<>() {
                            });

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

    public Collection getCollection(String collectionId, UUID appId) {
        Map<String, Object> body = new HashMap<>();
        body.put("collectionIds", singletonList(collectionId));
        if (appId != null) {
            body.put("appId", appId);
        }

        ResponseEntity<ApiCollections> response = layersApiRestTemplate
                .exchange(LAYERS_SEARCH_URI, HttpMethod.POST,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                        new ParameterizedTypeReference<>() {
                        });
        if (response.getBody() != null && !CollectionUtils.isEmpty(response.getBody().collections)) {
            return response.getBody().collections.get(0);
        }

        return null;
    }

    public Collection createCollection(LayerCreateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
                .exchange(COLLECTIONS_URI, HttpMethod.POST,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(dto),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public Collection updateCollection(String id, LayerUpdateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
                .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.PUT,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(dto),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public void deleteCollection(String id) {
        layersApiRestTemplate
                .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.DELETE,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null),
                        new ParameterizedTypeReference<>() {
                        });
    }

    public FeatureCollection updateLayerFeatures(String id, FeatureCollection body) {
        ResponseEntity<FeatureCollection> response = layersApiRestTemplate
                .exchange(String.format(UPDATE_FEATURES_URL, id), HttpMethod.PUT,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public FeatureCollection appendLayerFeaturesWithDefaultUser(String layerId, FeatureCollection body) {
        ResponseEntity<FeatureCollection> response = layersApiRestTemplate
                .exchange(String.format(UPDATE_FEATURES_URL, layerId), HttpMethod.POST,
                        httpEntityWithDefaultBearerAuth(body),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public LayersApiApplicationDto getApplicationLayers(UUID appId) {
        String urlTemplate = UriComponentsBuilder.fromUriString(String.format(APPS_URI, appId.toString()))
                .queryParam("includeDefaultCollections", "true")
                .encode()
                .toUriString();
        ResponseEntity<LayersApiApplicationDto> response = layersApiRestTemplate
                .exchange(urlTemplate, HttpMethod.GET,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public LayersApiApplicationDto updateApplicationLayers(UUID appId, List<AppLayerUpdateDto> layers) {
        ApplicationUpdateDto body = new ApplicationUpdateDto(false, true, layers);
        ResponseEntity<LayersApiApplicationDto> response = layersApiRestTemplate
                .exchange(String.format(APPS_URI, appId.toString()), HttpMethod.PUT,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public List<Feature> getCollectionFeatures(Geometry geoJson, String collectionId, UUID appId, int limit, int offset, String order) {
        Assert.notNull(collectionId, "Collection ID should not be null");

        Map<String, Object> body = new HashMap<>();
        if (geoJson != null) {
            body.put("geometry", geoJson);
        }
        if (appId != null) {
            body.put("appId", appId);
        }
        if (order != null) {
            body.put("order", order);
        }

        body.put("limit", limit);
        body.put("offset", offset);

        ResponseEntity<ApiFeatureCollection> response = layersApiRestTemplate
                .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, collectionId), HttpMethod.POST,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                        new ParameterizedTypeReference<>() {
                        });

        return response.getBody() != null ? response.getBody().getFeatures() : Collections.emptyList();
    }

    public List<Feature> getCollectionFeatures(Geometry geoJson, String collectionId, UUID appId) {
        Assert.notNull(collectionId, "Collection ID should not be null");

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("limit", pageSize);
        if (geoJson != null) {
            body.put("geometry", geoJson);
        }
        if (appId != null) {
            body.put("appId", appId);
        }

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiFeatureCollection> response = layersApiRestTemplate
                    .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, collectionId), HttpMethod.POST,
                            httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                            new ParameterizedTypeReference<>() {
                            });

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

    @Getter
    private static class ApplicationUpdateDto {

        private final boolean showAllPublicLayers;
        @JsonProperty("isPublic")
        private final boolean isPublic;
        private final List<AppLayerUpdateDto> layers;

        public ApplicationUpdateDto(boolean showAllPublicLayers, boolean isPublic, List<AppLayerUpdateDto> layers) {
            this.showAllPublicLayers = showAllPublicLayers;
            this.isPublic = isPublic;
            this.layers = layers;
        }
    }
}
