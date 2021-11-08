package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.NAME;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.layers.providers.OsmLayerProvider.getFeatureProperty;

@Service
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
        org.wololo.geojson.FeatureCollection urbanCoreAndSettledPeripheryLayers = insightsApiClient
            .getUrbanCoreAndSettledPeripheryLayers(geoJSON);
        return fromUrbanCoreAndPeripheryLayer(urbanCoreAndSettledPeripheryLayers, false);
    }

    /**
     * Returns null, as it's not possible to get urbanCore/settledPeriphery layers
     * without a geoJson boundary
     *
     * @return null
     */
    @Override
    public Layer obtainLayer(String layerId, UUID eventId) {
        return null;
    }

    @Override
    public boolean isApplicable(String layerId) {
        return providedLayers.contains(layerId);
    }

    List<Layer> fromUrbanCoreAndPeripheryLayer(org.wololo.geojson.FeatureCollection dto, boolean includeSourceData) {
        if (dto == null) {
            return List.of();
        }
        return Arrays.stream(dto.getFeatures()).map(f -> {
                Layer.LayerBuilder builder = Layer.builder()
                    .id((String) f.getId())
                    .name((String) f.getProperties().get(NAME))
                    .description(description(f));
                if (includeSourceData) {
                    builder.source(LayerSource.builder()
                        .type(GEOJSON)
                        .data(new FeatureCollection(new Feature[]{f}))
                        .build());
                }
                return builder.build();
            }
        ).collect(Collectors.toList());
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
