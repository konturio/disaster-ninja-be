package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.NAME;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.layers.providers.OsmLayerProvider.getFeatureProperty;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UrbanAndPeripheryLayerProvider implements LayerProvider {

    public static final String SETTLED_PERIPHERY_LAYER_ID = "kontur_settled_periphery";
    public static final String URBAN_CORE_LAYER_ID = "kontur_urban_core";
    private static final Logger LOG = LoggerFactory.getLogger(UrbanAndPeripheryLayerProvider.class);
    private static final Set<String> providedLayers = Set.of(SETTLED_PERIPHERY_LAYER_ID, URBAN_CORE_LAYER_ID);
    private final InsightsApiGraphqlClient insightsApiClient;

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    // TODO: retained for backward compatibility, remove later
    public CompletableFuture<List<Layer>> obtainLayers(LayerSearchParams searchParams) {
        if (searchParams.getBoundary() == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        return insightsApiClient
                .humanitarianImpactQuery(searchParams.getBoundary())
                .thenApply(fc -> providedLayers.stream()
                        .map(layerId -> urbanOrPeripheryLayer(fc, layerId, false))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams) {
        if (searchParams.getBoundary() == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        return insightsApiClient
                .humanitarianImpactQuery(searchParams.getBoundary())
                .thenApply(fc -> providedLayers.stream()
                        .map(layerId -> urbanOrPeripheryLayer(fc, layerId, false))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * @param layerId layer id to retrieve
     */
    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (searchParams.getBoundary() == null) {
            throw new WebApplicationException("GeoJson boundary must be specified for layer " + layerId,
                    HttpStatus.BAD_REQUEST);
        }
        try {
            return insightsApiClient
                    .humanitarianImpactQuery(searchParams.getBoundary())
                    .thenApply(fc -> urbanOrPeripheryLayer(fc, layerId, true))
                    .get();
        } catch (Exception e) {
            LOG.error("Can't load analytics data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    @Override
    public boolean isApplicable(String layerId) {
        return providedLayers.contains(layerId);
    }

    Layer urbanOrPeripheryLayer(org.wololo.geojson.FeatureCollection dto, String layerId, boolean includeSourceData) {
        if (dto == null || dto.getFeatures() == null || dto.getFeatures().length == 0) {
            return null;
        }
        return Arrays.stream(dto.getFeatures())
                .filter(Objects::nonNull)
                .filter(it -> layerId.equals(it.getId()))
                .findFirst()
                .map(f -> {
                    Layer.LayerBuilder builder = Layer.builder()
                            .id(layerId)
                            .name(getFeatureProperty(f, NAME, String.class))
                            .description(description(f));
                    if (includeSourceData) {
                        builder.source(LayerSource.builder()
                                .type(GEOJSON)
                                .data(new FeatureCollection(new Feature[]{f}))
                                .build());
                    }
                    return builder.build();
                }).orElse(null);
    }

    private String description(Feature f) {
        if (URBAN_CORE_LAYER_ID.equals(f.getId())) {
            return urbanCoreDescription(
                    getFeatureProperty(f, "population", Integer.class),
                    getFeatureProperty(f, "areaKm2", Double.class),
                    getFeatureProperty(f, "totalPopulation", Integer.class),
                    getFeatureProperty(f, "totalAreaKm2", Double.class)
            );
        }
        if (SETTLED_PERIPHERY_LAYER_ID.equals(f.getId())) {
            return settledPeripheryDescription(
                    getFeatureProperty(f, "population", Integer.class),
                    getFeatureProperty(f, "areaKm2", Double.class));
        }
        return null;
    }

    private String urbanCoreDescription(Integer population, Double areaKm2, Integer totalPopulation,
                                        Double totalAreaKm2) {
        return "Kontur Urban Core highlights most populated region affected. " +
                "For this event " + population + " people reside on " + areaKm2 + "km² (out of total " + totalPopulation +
                " people on " + totalAreaKm2 + "km²). This area should have higher priority in humanitarian activities.";
    }

    private String settledPeripheryDescription(Integer population, Double areaKm2) {
        return "Kontur Settled Periphery is complimentary to Kontur Urban Core and shows a spread-out" +
                " part of the population in the region. For this event it adds " + population + " people on " + areaKm2 +
                "km² on top of Kontur Urban Core.";
    }
}
