package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.DtoFeatureProperties;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Service
@Order(LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class OsmLayerProvider implements LayerProvider {

    private final KcApiClient kcApiClient;

    private static <T> T getMapValueFromProperty(Feature f, String propertyName, Object mapKey, Class<T> clazz) {
        Map map = getFeatureProperty(f, propertyName, Map.class);
        return map == null ? null : map.get(mapKey) == null ? null : clazz.cast(map.get(mapKey));
    }

    private static LayerCategory layerCategory(Feature f) {
        Boolean isOverlay = getFeatureProperty(f, DtoFeatureProperties.OVERLAY, Boolean.class);
        return (isOverlay != null && isOverlay) ? OVERLAY : BASE;
    }

    private static String caseFormat(String input) {
        return input == null ? null : input.toLowerCase().replace(input.substring(0, 1), input.substring(0, 1).toUpperCase());
    }

    protected static <T> T getFeatureProperty(Feature f, String propertyName, Class<T> clazz) {
        Object value = f.getProperties() == null ? null : (f.getProperties()).get(propertyName);
        if (value == null) {
            return null;
        }
        if (clazz == Integer.class && !(value instanceof Integer)) {
            value = ((Number) value).intValue();
        }
        return clazz.cast(value);
    }

    /**
     * @param geoJSON required to find features by intersection
     * @param eventId not used
     * @return
     */
    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        if (geoJSON == null) {
            return List.of();
        }
        List<Feature> osmLayers = kcApiClient.getOsmLayers(geoJSON);
        return fromOsmLayers(osmLayers);
    }

    /**
     * @param layerId LayerID to retrieve
     * @param eventId ignored
     * @return Entire layer, not limited by geometry
     */
    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        return fromOsmLayer(kcApiClient.getOsmLayer(layerId), true);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return true;
    }

    /**
     * Each feature from <b>dto</b> represents a separate layer in the result
     */
    List<Layer> fromOsmLayers(List<Feature> dto) {
        if (dto == null) {
            return List.of();
        }
        return dto.stream().filter(Objects::nonNull).map(it -> fromOsmLayer(it, false))
            .collect(Collectors.toList());
    }

    /**
     * A feature from represents a separate layer
     */
    Layer fromOsmLayer(Feature f, boolean includeSourceData) {
        if (f == null) {
            return null;
        }
        String copyright = getMapValueFromProperty(f, ATTRIBUTION, TEXT, String.class);
        Layer.LayerBuilder builder = Layer.builder()
            .id((String) f.getId())
            .name(getFeatureProperty(f, NAME, String.class))
            .description(getFeatureProperty(f, DESCRIPTION, String.class))
            .category(layerCategory(f))
            .group(caseFormat(getFeatureProperty(f, CATEGORY, String.class)))
            .copyrights(copyright == null ? null : List.of(copyright))
            .maxZoom(getFeatureProperty(f, MAX_ZOOM, Integer.class))
            .minZoom(getFeatureProperty(f, MIN_ZOOM, Integer.class));

        if (includeSourceData) {
            builder.source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(new Feature[]{f}))
                .build());
        }
        return builder.build();
    }
}
