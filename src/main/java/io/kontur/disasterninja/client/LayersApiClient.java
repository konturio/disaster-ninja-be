package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import io.kontur.disasterninja.dto.layerapi.Link;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
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
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.*;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_FEATURE;
import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_TILES;

@Component
public class LayersApiClient extends RestClientWithBearerAuth {
    public static final String LAYER_PREFIX = "KLA__";
    private static final Logger LOG = LoggerFactory.getLogger(LayersApiClient.class);

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

    public List<Layer> findLayers(Geometry geoJSON, CollectionOwner collectionOwner) {
        return getCollections(geoJSON, collectionOwner)
            .stream()
            .map(this::convertToLayer)
            .collect(Collectors.toList());
    }

    public Layer getLayer(Geometry geoJSON, String layerId) {
        String id = getIdWithoutPrefix(layerId);
        return convertToLayerDetails(geoJSON, getCollection(id));
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
                        httpEntityWithUserBearerAuthIfPresent(body), new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    private String getIdWithoutPrefix(String layerId) {
        return layerId.replaceFirst(LAYER_PREFIX, "");
    }

    private String getIdWithPrefix(String id) {
        return LAYER_PREFIX + id;
    }

    protected Collection createCollection(LayerCreateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(COLLECTIONS_URI, HttpMethod.POST, httpEntityWithUserBearerAuthIfPresent(dto),
                new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    protected Collection updateCollection(String id, LayerUpdateDto dto) {
        ResponseEntity<Collection> response = layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.PUT,
                httpEntityWithUserBearerAuthIfPresent(dto), new ParameterizedTypeReference<>() {
                });
        return response.getBody();
    }

    protected void deleteCollection(String id) {
        layersApiRestTemplate
            .exchange(String.format(COLLECTION_BY_ID_URL, id), HttpMethod.DELETE,
                httpEntityWithUserBearerAuthIfPresent(null), new ParameterizedTypeReference<>() {
                });
    }

    protected List<Collection> getCollections(Geometry geoJson, CollectionOwner collectionOwner) {
        List<Collection> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        if (collectionOwner != null) {
            body.put("collectionOwner", collectionOwner);
        }
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiCollections> response = layersApiRestTemplate
                .exchange(LAYERS_SEARCH_URI, HttpMethod.POST, httpEntityWithUserBearerAuthIfPresent(body),
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
                    httpEntityWithUserBearerAuthIfPresent(body),
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

    protected Collection getCollection(String collectionId) {
        ResponseEntity<Collection> response = layersApiRestTemplate
                .exchange(String.format(COLLECTION_BY_ID_URL, collectionId), HttpMethod.GET, httpEntityWithUserBearerAuthIfPresent(null),
                        new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    private Layer convertToLayer(Collection collection) {
        return Layer.builder()
            .id(getIdWithPrefix(collection.getId()))
            .name(collection.getTitle())
            .description(collection.getDescription())
            .category(collection.getCategory() != null ? LayerCategory.fromString(
                collection.getCategory().getName()) : null)
            .group(collection.getGroup() != null ? collection.getGroup().getName() : null)
            .legend(collection.getLegend() != null ? collection.getLegend().toLegend() : null)
            .copyrights(collection.getCopyrights() != null ? Collections.singletonList(collection.getCopyrights()) : null)
            .boundaryRequiredForRetrieval(!LAYER_TYPE_TILES.equals(collection.getItemType()) && !collection.isOwnedByUser())
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
            .urls(url != null ? Collections.singletonList(url) : null)
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
}
