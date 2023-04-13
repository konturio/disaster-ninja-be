package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.Style;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerType;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.LayersApiApplicationDto;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import io.kontur.disasterninja.dto.layerapi.Link;
import io.kontur.disasterninja.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.*;
import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_FEATURE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class LayersApiService {

    private final LayersApiClient layersApiClient;

    public List<Layer> findLayers(Geometry geoJson, boolean omitLocalLayers, CollectionOwner collectionOwner,
                                  UUID appId) {
        return layersApiClient.getCollections(geoJson, omitLocalLayers, collectionOwner, appId)
                .stream()
                .map((Collection collection) -> convertToLayer(collection, false, appId, null))
                .collect(Collectors.toList());
    }

    public Layer getLayer(Geometry geoJSON, String layerId, UUID appId) {
        Collection collection = layersApiClient.getCollection(layerId, appId);
        return convertToLayer(collection, true, appId, geoJSON);
    }

    public Layer createLayer(LayerCreateDto dto) {
        Collection collection = layersApiClient.createCollection(dto);
        return convertToLayer(collection, false, dto.getAppId(), null);
    }

    public Layer updateLayer(String layerId, LayerUpdateDto dto) {
        Collection collection = layersApiClient.updateCollection(layerId, dto);
        return convertToLayer(collection, false, dto.getAppId(), null);
    }

    public void deleteLayer(String layerId) {
        layersApiClient.deleteCollection(layerId);
    }

    public FeatureCollection updateFeatures(String layerId, FeatureCollection body) {
        return layersApiClient.updateLayerFeatures(layerId, body);
    }

    public List<Layer> getApplicationLayers(UUID appId) {
        LayersApiApplicationDto applicationDto = layersApiClient.getApplicationLayers(appId);
        return extractLayersFromApplication(appId, applicationDto);
    }

    public List<Layer> updateApplicationLayers(UUID appId, List<AppLayerUpdateDto> layers) {
        LayersApiApplicationDto applicationDto = layersApiClient.updateApplicationLayers(appId, layers);
        return extractLayersFromApplication(appId, applicationDto);
    }

    private List<Layer> extractLayersFromApplication(UUID appId, LayersApiApplicationDto applicationDto) {
        if (applicationDto == null || CollectionUtils.isEmpty(applicationDto.getDefaultCollections())) {
            return emptyList();
        }
        return applicationDto.getDefaultCollections()
                .stream()
                .map((Collection collection) -> convertToLayer(collection, true, appId, null))
                .collect(Collectors.toList());
    }

    private Layer convertToLayer(Collection collection, boolean includeSource, UUID appId, Geometry geoJSON) {
        if (collection == null) {
            return null;
        }
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

        Layer.LayerBuilder builder = Layer.builder()
                .id(collection.getId())
                .name(collection.getTitle())
                .description(collection.getDescription())
                .category(collection.getCategory() != null ? LayerCategory.fromString(
                        collection.getCategory().getName()) : null)
                .group(collection.getGroup() != null ? collection.getGroup().getName() : null)
                .legend(collection.getStyleRule() != null ?
                        JsonUtil.readObjectNode(collection.getStyleRule(), Legend.class) : null)
                .style(collection.getStyle() != null ?
                        JsonUtil.readObjectNode(collection.getStyle(), Style.class) : null)
                .copyrights(collection.getCopyrights() != null ? singletonList(collection.getCopyrights()) : null)
                .boundaryRequiredForRetrieval(boundaryRequiredForRetrieval)
                .eventIdRequiredForRetrieval(eventIdRequiredForRetrieval)
                .ownedByUser(collection.isOwnedByUser())
                .properties(collection.getProperties())
                .featureProperties(collection.getFeatureProperties())
                .type(LayerType.fromString(collection.getItemType()))
                .mapboxStyles(collection.getMapboxStyles())
                .popupConfig(collection.getPopupConfig());

        if (includeSource) {
            LayerSource source = null;
            if (collection.getItemType() != null) {
                source = switch (collection.getItemType()) {
                    /*
                    All special cases will be considered first.
                    For some layer types 'data' will be provided instead of 'url'.
                    For some layer types 'tileSize' might be required.
                    New layer types will be supported in #15281.
                     */
                    case (LAYER_TYPE_VECTOR), (LAYER_TYPE_TILES) -> createVectorSource(collection);
                    case (LAYER_TYPE_RASTER) -> createRasterSource(collection);
                    case (LAYER_TYPE_FEATURE) -> createFeatureSource(geoJSON, collection.getId(), appId);
                    /*
                      By default, each layer type will require 'url'.
                     */
                    default -> createDefaultSource(collection);
                };
            }
            builder.minZoom(collection.getMinZoom())
                    .maxZoom(collection.getMaxZoom())
                    .source(source);
        }
        return builder.build();
    }

    private LayerSource createVectorSource(Collection collection) {
        String url = getSourceUrl(collection);
        String apiKey = getSourceApiKey(collection);

        return LayerSource.builder()
                .type(LayerType.VECTOR)
                .tileSize(collection.getTileSize() != null ? collection.getTileSize() : 512)
                .urls(url != null ? singletonList(url) : null)
                .apiKey(apiKey)
                .build();
    }

    private LayerSource createRasterSource(Collection collection) {
        String url = getSourceUrl(collection);
        String apiKey = getSourceApiKey(collection);

        return LayerSource.builder()
                .type(LayerType.RASTER)
                .tileSize(collection.getTileSize() != null ? collection.getTileSize() : 256)
                .urls(url != null ? singletonList(url) : null)
                .apiKey(apiKey)
                .build();
    }

    private LayerSource createFeatureSource(Geometry geoJSON, String layerId, UUID appId) {
        List<Feature> features = layersApiClient.getCollectionFeatures(geoJSON, layerId, appId);
        return LayerSource.builder()
                .type(LayerType.GEOJSON)
                .data(new FeatureCollection(features.toArray(new Feature[0])))
                .build();
    }

    private LayerSource createDefaultSource(Collection collection) {
        String url = getSourceUrl(collection);
        String apiKey = getSourceApiKey(collection);

        return LayerSource.builder()
                .type(LayerType.fromString(collection.getItemType()))
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
}
