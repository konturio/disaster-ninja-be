package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import io.kontur.disasterninja.dto.layerapi.Link;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.kontur.disasterninja.util.JsonUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;

import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_FEATURE;
import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_TILES;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class LayersApiClient extends RestClientWithBearerAuth {

    public static final String LAYER_PREFIX = "KLA__";
    private static final Logger LOG = LoggerFactory.getLogger(LayersApiClient.class);

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

    public List<Layer> findLayers(Geometry geoJSON, CollectionOwner collectionOwner, UUID appId) {
        return getCollections(geoJSON, collectionOwner, appId)
                .stream()
                .map(this::convertToLayer)
                .collect(Collectors.toList());
    }

    public Layer getLayer(Geometry geoJSON, String layerId, UUID appId) {
        String id = getIdWithoutPrefix(layerId);
        return convertToLayerDetails(geoJSON, getCollection(id, appId));
    }

    public Layer createLayer(LayerCreateDto dto) {
        Collection collection = createCollection(dto);
        return convertToLayer(collection);
    }

    public Layer updateLayer(String layerId, LayerUpdateDto dto) {
        String id = getIdWithoutPrefix(layerId);
        Collection collection = updateCollection(id, dto);
        return convertToLayer(collection);
    }

    public void deleteLayer(String layerId) {
        String id = getIdWithoutPrefix(layerId);
        deleteCollection(id);
    }

    public FeatureCollection updateLayerFeatures(String layerId, FeatureCollection body) {
        String id = getIdWithoutPrefix(layerId);

        ResponseEntity<FeatureCollection> response = layersApiRestTemplate
            .exchange(String.format(UPDATE_FEATURES_URL, id), HttpMethod.PUT,
                httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body), new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    public List<Layer> getApplicationLayers(UUID appId) {
        String urlTemplate = UriComponentsBuilder.fromUriString(String.format(APPS_URI, appId.toString()))
                .queryParam("includeDefaultCollections", "true")
                .encode()
                .toUriString();
        ResponseEntity<ApplicationDto> response = layersApiRestTemplate
            .exchange(urlTemplate, HttpMethod.GET,
                httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null), new ParameterizedTypeReference<>() {
                });
        ApplicationDto body = response.getBody();
        if (body == null || CollectionUtils.isEmpty(body.getDefaultCollections())) {
            return emptyList();
        }
        return body.getDefaultCollections()
                .stream()
                .map(this::convertToLayer)
                .collect(Collectors.toList());
    }

    public List<Layer> updateApplicationLayers(UUID appId, List<AppLayerUpdateDto> layers) {
        List<AppLayerUpdateDto> layersToUpdate = layers.stream()
                .map(l -> new AppLayerUpdateDto(getIdWithoutPrefix(l.getLayerId()), l.getIsDefault(), l.getStyleRule()))
                .toList();

        ResponseEntity<ApplicationDto> response = layersApiRestTemplate
                .exchange(String.format(APPS_URI, appId.toString()), HttpMethod.PUT,
                    httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(new ApplicationUpdateDto(false,
                        true, layersToUpdate)), new ParameterizedTypeReference<>() {
                    });
        ApplicationDto body = response.getBody();
        if (body == null || CollectionUtils.isEmpty(body.getDefaultCollections())) {
            return emptyList();
        }
        return body.getDefaultCollections()
                .stream()
                .map(this::convertToLayer)
                .collect(Collectors.toList());
    }

    private String getIdWithoutPrefix(String layerId) {
        return layerId.replaceFirst(LAYER_PREFIX, "");
    }

    private String getIdWithPrefix(String id) {
        return LAYER_PREFIX + id;
    }

    protected Collection createCollection(LayerCreateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(COLLECTIONS_URI, HttpMethod.POST, httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(dto),
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    protected Collection updateCollection(String id, LayerUpdateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.PUT,
                httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(dto), new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    protected void deleteCollection(String id) {
        layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.DELETE,
                httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null), new ParameterizedTypeReference<>() {
                });
    }

    protected List<Collection> getCollections(Geometry geoJson, CollectionOwner collectionOwner, UUID appId) {
        List<Collection> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        if (collectionOwner != null) {
            body.put("collectionOwner", collectionOwner);
        }
        body.put("geometry", geoJson);
        body.put("limit", pageSize);
        if (appId == null) {
            //TODO remove default value after DN2 FE is updated with application functionality
            body.put("appId", UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb54"));
        } else {
            body.put("appId", appId);
        }

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiCollections> response = layersApiRestTemplate
                .exchange(LAYERS_SEARCH_URI, HttpMethod.POST,
                    httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body), new ParameterizedTypeReference<>() {
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

    public List<Feature> getCollectionFeatures(Geometry geoJson, String collectionId) {
        Assert.notNull(collectionId, "Collection ID should not be null");

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiFeatureCollection> response = layersApiRestTemplate
                .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, collectionId), HttpMethod.POST,
                    httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body), new ParameterizedTypeReference<>() {
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

    protected Collection getCollection(String collectionId, UUID appId) {
        Map<String, Object> body = new HashMap<>();
        body.put("collectionIds", singletonList(collectionId));
        if (appId == null) {
            //TODO remove default value after DN2 FE is updated with application functionality
            body.put("appId", UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb54"));
        } else {
            body.put("appId", appId);
        }

        ResponseEntity<ApiCollections> response = layersApiRestTemplate
            .exchange(LAYERS_SEARCH_URI, HttpMethod.POST, httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                new ParameterizedTypeReference<>() {
                });
        if (response.getBody() != null && !CollectionUtils.isEmpty(response.getBody().collections)) {
            return response.getBody().collections.get(0);
        }
        return null;
    }

    private Layer convertToLayer(Collection collection) {
        return Layer.builder()
                .id(getIdWithPrefix(collection.getId()))
                .name(collection.getTitle())
                .description(collection.getDescription())
                .category(collection.getCategory() != null ? LayerCategory.fromString(
                        collection.getCategory().getName()) : null)
                .group(collection.getGroup() != null ? collection.getGroup().getName() : null)
                .legend(collection.getStyleRule() != null ?
                        JsonUtil.readObjectNode(collection.getStyleRule(), Legend.class) : null)
                .copyrights(collection.getCopyrights() != null ? singletonList(collection.getCopyrights()) : null)
                .boundaryRequiredForRetrieval(
                        !LAYER_TYPE_TILES.equals(collection.getItemType()) && !collection.isOwnedByUser())
                .eventIdRequiredForRetrieval(false)
                .ownedByUser(collection.isOwnedByUser())
                .featureProperties(collection.getFeatureProperties())
                .build();
    }

    private Layer convertToLayerDetails(Geometry geoJSON,
                                        Collection collection) {
        if (collection == null) {
            return null;
        }
        LayerSource source = null;
        if (LAYER_TYPE_TILES.equals(collection.getItemType())) {
            source = createVectorSource(collection);
        } else if (LAYER_TYPE_FEATURE.equals(collection.getItemType())) {
            source = createFeatureSource(geoJSON, collection.getId());
        }
        return Layer.builder()
                .id(getIdWithPrefix(collection.getId()))
                .legend(collection.getStyleRule() != null ?
                        JsonUtil.readObjectNode(collection.getStyleRule(), Legend.class) : null)
                .source(source)
                .ownedByUser(collection.isOwnedByUser())
                .build();
    }

    private LayerSource createVectorSource(Collection collection) {
        String url = collection.getLinks().stream()
                .filter(l -> LAYER_TYPE_TILES.equals(l.getRel()))
                .map(Link::getHref)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return LayerSource.builder()
                .type(LayerSourceType.VECTOR)
                .tileSize(512)
                .urls(url != null ? singletonList(url) : null)
                .build();
    }

    private LayerSource createFeatureSource(Geometry geoJSON, String layerId) {
        List<Feature> features = getCollectionFeatures(geoJSON, layerId);
        return LayerSource.builder()
                .type(LayerSourceType.GEOJSON)
                .data(new FeatureCollection(features.toArray(new Feature[0])))
                .build();
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
    private static class ApplicationDto {

        private final UUID id;
        private final boolean showAllPublicLayers;
        private final boolean isPublic;
        private final List<Collection> defaultCollections;

        @JsonCreator
        public ApplicationDto(@JsonProperty("id") UUID id,
                              @JsonProperty("showAllPublicLayers") boolean showAllPublicLayers,
                              @JsonProperty("isPublic") boolean isPublic,
                              @JsonProperty("defaultCollections") List<Collection> defaultCollections) {
            this.id = id;
            this.showAllPublicLayers = showAllPublicLayers;
            this.isPublic = isPublic;
            this.defaultCollections = defaultCollections;
        }
    }

    @Getter
    private static class ApplicationUpdateDto {

        private final boolean showAllPublicLayers;
        @JsonProperty("isPublic")
        private final boolean isPublic;
        private final List<AppLayerUpdateDto> layers;

        public ApplicationUpdateDto(boolean showAllPublicLayers, boolean isPublic,
                                    List<AppLayerUpdateDto> layers) {
            this.showAllPublicLayers = showAllPublicLayers;
            this.isPublic = isPublic;
            this.layers = layers;
        }
    }
}
