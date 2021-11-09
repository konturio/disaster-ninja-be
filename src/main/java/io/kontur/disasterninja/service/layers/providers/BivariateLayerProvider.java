package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import io.kontur.disasterninja.service.layers.LayerConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
import static io.kontur.disasterninja.domain.enums.LegendType.BIVARIATE;

@Service
@RequiredArgsConstructor
public class BivariateLayerProvider implements LayerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BivariateLayerProvider.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;
    private final LayerConfigService layerConfigService;
    private List<String> bivariateLayerIds;

    @PostConstruct
    private void init() {
        bivariateLayerIds = layerConfigService.getGlobalOverlays().values().stream()
            .filter(it -> {
                return it.getLegend() != null && it.getLegend().getType() == BIVARIATE;
            }).map(Layer::getId).collect(Collectors.toList());
    }


    private static String notEmptyLabel(Supplier<String> labelSupplier) {
        if (labelSupplier.get() != null && !labelSupplier.get().isBlank()) {
            return labelSupplier.get();
        }
        return null;
    }

    /**
     * @param geoJSON not used
     * @param eventId not used
     * @return Bivariate layers from insights-api graphql api
     */
    @Override
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        try {
            return insightsApiGraphqlClient.getBivariateOverlays().get()
                .stream().map(this::fromOverlay).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Can't load bivariate layers: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isApplicable(String layerId) {
        return bivariateLayerIds.contains(layerId);
    }

    protected Layer fromOverlay(BivariateLayerLegendQuery.Overlay overlay) {
        if (overlay == null) {
            return null;
        }
        return Layer.builder()
            .id(overlay.name())
            .description(overlay.description())
            .source(LayerSource.builder()
                .type(VECTOR)
                .build())
            .legend(bivariateLegendFromOverlay(overlay))
            .build();
    }

    /**
     * @param geoJSON not used
     * @param layerId layer id to retrieve
     * @param eventId not used
     * @return Bivariate layer by ID from insights-api graphql api
     */
    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        try {
            return insightsApiGraphqlClient.getBivariateOverlays().get()
                .stream()
                .filter(it -> layerId.equals(it.name())).findFirst()
                .map(this::fromOverlay).get();
        } catch (InterruptedException | ExecutionException | NoSuchElementException e) {
            LOG.error("Can't load bivariate layer by id {}: {}", layerId, e.getMessage(), e);
        }
        return null;
    }

    private Legend bivariateLegendFromOverlay(BivariateLayerLegendQuery.Overlay overlay) {
        if (overlay == null) {
            return null;
        }
        List<LegendStep> resultingSteps = new ArrayList<>();
        //AXIS 1
        BivariateLayerLegendQuery.X x = overlay.x();
        if (x != null && x.steps() != null) {
            for (int i = 0; i < x.steps().size(); i++) {
                BivariateLayerLegendQuery.Step step = x.steps().get(i);
                LegendStep legendStep = new LegendStep(null, null, "X", step.value(),
                    notEmptyLabel(step::label), null, null);
                legendStep.setOrder(i);
                resultingSteps.add(legendStep);
            }
        }
        //AXIS 2
        BivariateLayerLegendQuery.Y y = overlay.y();
        if (y != null && y.steps() != null) {
            for (int i = 0; i < y.steps().size(); i++) {
                BivariateLayerLegendQuery.Step1 step = y.steps().get(i);
                LegendStep legendStep = new LegendStep(null, null, "Y", step.value(),
                    notEmptyLabel(step::label), null, null);
                legendStep.setOrder(i);
                resultingSteps.add(legendStep);
            }
        }
        //colors matrix
        Map<String, String> colors = overlay.colors().stream().collect(Collectors.toMap(color -> color.id(),
            color -> color.color()));

        return Legend.builder()
            .type(BIVARIATE)
            .steps(resultingSteps)
            .bivariateColors(colors)
            .build();
    }
}
