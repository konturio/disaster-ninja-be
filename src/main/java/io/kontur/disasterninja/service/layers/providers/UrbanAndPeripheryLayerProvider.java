package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.*;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.NAME;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.layers.providers.OsmLayerProvider.getFeatureProperty;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UrbanAndPeripheryLayerProvider implements LayerProvider {
    private static final Set<String> providedLayers = Set.of(SETTL_PERIPHERY_LAYER_ID, URBAN_CORE_LAYER_ID);

    private final InsightsApiClient insightsApiClient;

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
        org.wololo.geojson.FeatureCollection features = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJSON);
        return providedLayers.stream().map(layerId -> urbanOrPeripheryLayer(features, layerId, false))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * @param geoJson required to find features by intersection
     * @param layerId layer id to retrieve
     * @param eventId not used
     * @return
     */
    @Override
    public Layer obtainLayer(Geometry geoJson, String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (geoJson == null) {
            throw new WebApplicationException("GeoJson boundary must be specified for layer " + layerId,
                HttpStatus.BAD_REQUEST);
        }
        org.wololo.geojson.FeatureCollection features = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJson);

        return urbanOrPeripheryLayer(features, layerId, true);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return providedLayers.contains(layerId);
    }

    Layer urbanOrPeripheryLayer(org.wololo.geojson.FeatureCollection dto, String layerId, boolean includeSourceData) {
        if (dto == null || dto.getFeatures() == null || dto.getFeatures().length == 0) {
            return null;
        }
        return Arrays.stream(dto.getFeatures()).filter(it -> layerId.equals(it.getId()))
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
            }).get();
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
        if (SETTL_PERIPHERY_LAYER_ID.equals(f.getId())) {
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
