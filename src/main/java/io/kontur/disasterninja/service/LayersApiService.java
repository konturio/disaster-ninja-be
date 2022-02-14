package io.kontur.disasterninja.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.Link;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

@Service
@RequiredArgsConstructor
public class LayersApiService {
    public static final String LAYER_PREFIX = "KLA__";
    private static final Logger LOG = LoggerFactory.getLogger(LayersApiService.class);
    private final LayersApiClient layersApiClient;

    public Layer createLayer(LayerCreateDto dto) {
        Collection collection = layersApiClient.createCollection(dto);
        return convertToLayer(collection);
    }

    public Layer updateLayer(String id, LayerUpdateDto dto) {
        Collection collection = layersApiClient.updateCollection(id, dto);
        return convertToLayer(collection);
    }

    public void deleteLayer(String id) {
        layersApiClient.deleteCollection(id);
    }

    public List<Layer> findLayers(Geometry geoJSON) {
        return layersApiClient.getCollections(geoJSON)
            .stream()
            .map(this::convertToLayer)
            .collect(Collectors.toList());
    }

    public Layer getLayer(Geometry geoJSON, String layerId, UUID eventId) {
        String id = layerId.replaceFirst(LAYER_PREFIX, "");
        return convertToLayerDetails(geoJSON, layersApiClient.getCollection(id));
    }

    private Layer convertToLayer(Collection collection) {
        return Layer.builder()
            .id(LAYER_PREFIX + collection.getId())
            .name(collection.getTitle())
            .description(collection.getDescription())
            .category(collection.getCategory() != null ? LayerCategory.fromString(
                collection.getCategory().getName()) : null)
            .group(collection.getGroup() != null ? collection.getGroup().getName() : null)
            .legend(collection.getLegend() != null ? collection.getLegend().toLegend() : null)
            .copyrights(Collections.singletonList(collection.getCopyrights()))
            .boundaryRequiredForRetrieval(!"tiles".equals(collection.getItemType()))
            .eventIdRequiredForRetrieval(false)
            .build();
    }

    private Layer convertToLayerDetails(Geometry geoJSON,
                                        Collection collection) {
        if (collection == null) {
            return null;
        }
        LayerSource source = null;
        if ("tiles".equals(collection.getItemType())) {
            source = createVectorSource(collection);
        } else if ("feature".equals(collection.getItemType())) {
            source = createFeatureSource(geoJSON, collection.getId());
        }
        return Layer.builder()
            .id(LAYER_PREFIX + collection.getId())
            .source(source)
            .build();
    }

    private LayerSource createVectorSource(Collection collection) {
        return LayerSource.builder()
            .type(LayerSourceType.VECTOR)
            .tileSize(512)
            .urls(Collections.singletonList(
                collection.getLinks().stream()
                    .filter(l -> "tiles".equals(l.getRel()))
                    .map(Link::getHref)
                    .findFirst()
                    .orElse(null)))
            .build();
    }

    private LayerSource createFeatureSource(Geometry geoJSON, String layerId) {
        List<Feature> features = layersApiClient.getCollectionFeatures(geoJSON, layerId);
        return LayerSource.builder()
            .type(LayerSourceType.GEOJSON)
            .data(new FeatureCollection(features.toArray(new Feature[0])))
            .build();
    }

}
