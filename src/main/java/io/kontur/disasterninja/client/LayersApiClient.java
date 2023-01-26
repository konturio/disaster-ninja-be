package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.dto.application.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.application.LayersApiAppDto;
import io.kontur.disasterninja.dto.application.LayersApiAppUpdateDto;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import io.kontur.disasterninja.dto.layerapi.Link;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.kontur.disasterninja.util.JsonUtil;
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
import java.util.stream.Collectors;

import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class LayersApiClient extends RestClientWithBearerAuth {

    public static final String LAYER_PREFIX = "KLA__";

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

    public List<Layer> findLayers(Geometry geoJSON, boolean omitLocalLayers, CollectionOwner collectionOwner, UUID appId) {
        return getCollections(geoJSON, omitLocalLayers, collectionOwner, appId)
                .stream()
                .map(this::convertToLayer)
                .collect(Collectors.toList());
    }

    public Layer getLayer(Geometry geoJSON, String layerId, UUID appId) {
        String id = getIdWithoutPrefix(layerId);
        Collection collection = getCollection(id, appId);
        return convertToLayerDetails(geoJSON, collection, appId);
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
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(body),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public LayersApiAppDto getApp(UUID appId) {
        String urlTemplate = UriComponentsBuilder.fromUriString(String.format(APPS_URI, appId.toString()))
                .queryParam("includeDefaultCollections", "true")
                .encode()
                .toUriString();
        ResponseEntity<LayersApiAppDto> response = layersApiRestTemplate
                .exchange(urlTemplate, HttpMethod.GET,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null),
                        new ParameterizedTypeReference<>() {
                        });
        return response.getBody();
    }

    public List<Layer> getApplicationLayers(UUID appId) {
        LayersApiAppDto body = getApp(appId);
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

        ResponseEntity<LayersApiAppDto> response = layersApiRestTemplate
                .exchange(String.format(APPS_URI, appId.toString()), HttpMethod.PUT,
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(new LayersApiAppUpdateDto(false,
                                true, layersToUpdate)), new ParameterizedTypeReference<>() {
                        });
        LayersApiAppDto body = response.getBody();
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
                        httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(null),
                        new ParameterizedTypeReference<>() {
                        });
    }

    protected List<Collection> getCollections(Geometry geoJson, boolean omitLocalLayers, CollectionOwner collectionOwner, UUID appId) {
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

    public List<Feature> getCollectionFeatures(Geometry geoJson, String collectionId, UUID appId) {
        Assert.notNull(collectionId, "Collection ID should not be null");

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);
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

    protected Collection getCollection(String collectionId, UUID appId) {
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

    public Layer convertToLayer(Collection collection) {
        boolean boundaryRequiredForRetrieval = !LAYER_TYPE_TILES.equals(
                collection.getItemType()) && !collection.isOwnedByUser();
        if (collection.getDisplayRule() != null &&
                collection.getDisplayRule().get("boundaryRequiredForRetrieval") != null) {
            boundaryRequiredForRetrieval = collection.getDisplayRule().get("boundaryRequiredForRetrieval").asBoolean();
        }

        boolean eventIdRequiredForRetrieval = false;
        if (collection.getDisplayRule() != null &&
                collection.getDisplayRule().get("eventIdRequiredForRetrieval") != null) {
            eventIdRequiredForRetrieval = collection.getDisplayRule().get("eventIdRequiredForRetrieval").asBoolean();
        }

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
                .boundaryRequiredForRetrieval(boundaryRequiredForRetrieval)
                .eventIdRequiredForRetrieval(eventIdRequiredForRetrieval)
                .ownedByUser(collection.isOwnedByUser())
                .featureProperties(collection.getFeatureProperties())
                .build();
    }

    public Layer convertToLayerDetails(Geometry geoJSON, Collection collection, UUID appId) {
        if (collection == null) {
            return null;
        }
        LayerSource source = switch (collection.getItemType()) {
            case (LAYER_TYPE_VECTOR), (LAYER_TYPE_TILES) -> createVectorSource(collection);
            case (LAYER_TYPE_RASTER) -> createRasterSource(collection);
            case (LAYER_TYPE_FEATURE) -> createFeatureSource(geoJSON, collection.getId(), appId);
            default -> null;
        };
        return Layer.builder()
                .id(getIdWithPrefix(collection.getId()))
                .legend(collection.getStyleRule() != null ?
                        JsonUtil.readObjectNode(collection.getStyleRule(), Legend.class) : null)
                .minZoom(collection.getMinZoom())
                .maxZoom(collection.getMaxZoom())
                .source(source)
                .ownedByUser(collection.isOwnedByUser())
                .build();
    }

    private LayerSource createVectorSource(Collection collection) {
        String url = getSourceUrl(collection);
        String apiKey = getSourceApiKey(collection);

        return LayerSource.builder()
                .type(LayerSourceType.VECTOR)
                .tileSize(collection.getTileSize() != null ? collection.getTileSize() : 512)
                .urls(url != null ? singletonList(url) : null)
                .apiKey(apiKey)
                .build();
    }

    private LayerSource createRasterSource(Collection collection) {
        String url = getSourceUrl(collection);
        String apiKey = getSourceApiKey(collection);

        return LayerSource.builder()
                .type(LayerSourceType.RASTER)
                .tileSize(collection.getTileSize() != null ? collection.getTileSize() : 256)
                .urls(url != null ? singletonList(url) : null)
                .apiKey(apiKey)
                .build();
    }

    private String getSourceUrl(Collection collection) {
        return collection.getLinks().stream()
                .filter(l -> LAYER_TYPE_TILES.equals(l.getRel()))
                .map(Link::getHref)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String getSourceApiKey(Collection collection) {
        return collection.getLinks().stream()
                .filter(l -> LAYER_TYPE_TILES.equals(l.getRel()))
                .map(Link::getApiKey)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private LayerSource createFeatureSource(Geometry geoJSON, String layerId, UUID appId) {
        List<Feature> features = getCollectionFeatures(geoJSON, layerId, appId);
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
