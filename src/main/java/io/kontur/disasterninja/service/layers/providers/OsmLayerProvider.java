package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.DtoFeatureProperties;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
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
        return value == null ? null : clazz.cast(value);
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

    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        throw new NotImplementedException(); //todo #7385 + javadoc
    }

    @Override
    public boolean isApplicable(String layerId) {
        return true; //todo #7385
    }

    /**
     * Each feature from <b>dto</b> represents a separate layer in the result
     */
    List<Layer> fromOsmLayers(List<Feature> dto) {
        if (dto == null) {
            return List.of();
        }
        return dto.stream().map(f -> {
                    String copyright = getMapValueFromProperty(f, ATTRIBUTION, TEXT, String.class);
                    return Layer.builder()
                        .id((String) f.getId())
                        .name(getFeatureProperty(f, NAME, String.class))
                        .description(getFeatureProperty(f, DESCRIPTION, String.class))
                        .category(layerCategory(f))
                        .group(caseFormat(getFeatureProperty(f, CATEGORY, String.class)))
                        .copyrights(copyright == null ? null : List.of(copyright))
                        .maxZoom(getFeatureProperty(f, MAX_ZOOM, Integer.class))
                        .minZoom(getFeatureProperty(f, MIN_ZOOM, Integer.class))
                        .build();
                }
            )
            .collect(Collectors.toList());
    }
}
