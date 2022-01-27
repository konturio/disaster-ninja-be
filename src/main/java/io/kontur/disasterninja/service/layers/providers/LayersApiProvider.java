package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
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
        return layersApiClient.getLayers(geoJSON)
                .stream()
                .map(this::convertLayer)
                .collect(Collectors.toList());
    }

    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        String id = layerId.replaceFirst(LAYER_PREFIX, "");
        return convertLayerDetails(geoJSON, layersApiClient.getLayer(id));
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
    }

    private Layer convertLayer(io.kontur.disasterninja.dto.layerapi.Layer layer) {
        return Layer.builder()
                .id(LAYER_PREFIX + layer.getId())
                .name(layer.getTitle())
                .description(layer.getDescription())
                .category(layer.getCategory() != null ? LayerCategory.fromString(layer.getCategory().getName()) : null)
                .group(layer.getGroup() != null ? layer.getGroup().getName() : null)
                .legend(layer.getLegend())
                .copyrights(Collections.singletonList(layer.getCopyrights()))
                .boundaryRequiredForRetrieval(!"tiles".equals(layer.getItemType()))
                .eventIdRequiredForRetrieval(false)
                .build();
    }

    private Layer convertLayerDetails(Geometry geoJSON,
                                      io.kontur.disasterninja.dto.layerapi.Layer layer) {
        if (layer == null) {
            return null;
        }
        LayerSource source = null;
        if ("tiles".equals(layer.getItemType())) {
            source = createVectorSource(layer);
        } else if ("feature".equals(layer.getItemType())) {
            source = createFeatureSource(geoJSON, layer.getId());
        }
        return Layer.builder()
                .id(LAYER_PREFIX + layer.getId())
                .source(source)
                .build();
    }

    private LayerSource createVectorSource(io.kontur.disasterninja.dto.layerapi.Layer layer) {
        return LayerSource.builder()
                .type(LayerSourceType.VECTOR)
                .tileSize(512)
                .urls(Collections.singletonList(
                        layer.getLinks().stream()
                                .filter(l -> "tiles".equals(l.getRel()))
                                .map(Link::getHref)
                                .findFirst()
                                .orElse(null)))
                .build();
    }

    private LayerSource createFeatureSource(Geometry geoJSON, String layerId) {
        List<Feature> features = layersApiClient.getLayersFeatures(geoJSON, layerId);
        return LayerSource.builder()
                .type(LayerSourceType.GEOJSON)
                .data(new FeatureCollection(features.toArray(new Feature[0])))
                .build();
    }
}
