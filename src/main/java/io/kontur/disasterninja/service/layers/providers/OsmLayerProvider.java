package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.DtoFeatureProperties;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.service.layers.LocalLayerConfigService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.client.KcApiClient.OSM_LAYERS;
import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.RASTER;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Service
@Order(LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class OsmLayerProvider implements LayerProvider {

    private final KcApiClient kcApiClient;
    private final LocalLayerConfigService layerConfigService;
    private Set<String> globalOverlays;

    @PostConstruct
    private void init() {
        globalOverlays = layerConfigService.getGlobalOverlays().keySet();
    }

    private static <T> T getMapValueFromProperty(Feature f, String propertyName, Object mapKey, Class<T> clazz) {
        Map map = getFeatureProperty(f, propertyName, Map.class);
        return map == null ? null : map.get(mapKey) == null ? null : clazz.cast(map.get(mapKey));
    }

    private static LayerCategory layerCategory(Feature f) {
        Boolean isOverlay = getFeatureProperty(f, DtoFeatureProperties.OVERLAY, Boolean.class);
        return (isOverlay != null && isOverlay) ? OVERLAY : BASE;
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

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainLayers(LayerSearchParams searchParams) {
        List<Feature> osmLayers = kcApiClient.getCollectionItemsByGeometry(searchParams.getBoundary(), OSM_LAYERS);
        return CompletableFuture.completedFuture(fromOsmLayers(osmLayers));
    }

    /**
     * @param layerId LayerID to retrieve
     * @return Entire layer, not limited by geometry
     */
    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        return fromOsmLayer(kcApiClient.getFeatureFromCollection(searchParams.getBoundary(), layerId, OSM_LAYERS),
                true);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return !globalOverlays.contains(layerId);
    }

    /**
     * Each feature from <b>dto</b> represents a separate layer in the result
     */
    List<Layer> fromOsmLayers(List<Feature> dto) {
        if (dto == null) {
            return Collections.emptyList();
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
                .group(getFeatureProperty(f, CATEGORY, String.class))
                .copyrights(copyright == null ? null : List.of(copyright))
                .maxZoom(getFeatureProperty(f, MAX_ZOOM, Integer.class))
                .minZoom(getFeatureProperty(f, MIN_ZOOM, Integer.class));

        if (includeSourceData) {
            String url = getFeatureProperty(f, URL, String.class);

            builder.source(LayerSource.builder()
                    .type(RASTER)
                    .urls(url != null ? List.of(url) : null)

                    .data(new FeatureCollection(new Feature[]{f}))
                    .build());
        }
        return builder.build();
    }
}
