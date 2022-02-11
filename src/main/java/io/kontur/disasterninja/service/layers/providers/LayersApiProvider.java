package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.dto.layerapi.Link;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
public class LayersApiProvider implements LayerProvider {

    private static final String LAYER_PREFIX = "KLA__";
    private final LayersApiClient layersApiClient;

    public LayersApiProvider(LayersApiClient layersApiClient) {
        this.layersApiClient = layersApiClient;
    }

    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        return layersApiClient.getCollections(geoJSON)
                .stream()
                .map(this::convertToLayer)
                .collect(Collectors.toList());
    }

    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        String id = layerId.replaceFirst(LAYER_PREFIX, "");
        return convertToLayerDetails(geoJSON, layersApiClient.getCollection(id));
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
    }

    private Layer convertToLayer(Collection collection) {
        return Layer.builder()
                .id(LAYER_PREFIX + collection.getId())
                .name(collection.getTitle())
                .description(collection.getDescription())
                .category(collection.getCategory() != null ? LayerCategory.fromString(
                    collection.getCategory().getName()) : null)
                .group(collection.getGroup() != null ? collection.getGroup().getName() : null)
                .legend(collection.getLegend())
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
