package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
import static io.kontur.disasterninja.domain.enums.LegendType.BIVARIATE;

@Service
@RequiredArgsConstructor
public class BivariateLayerProvider implements LayerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BivariateLayerProvider.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

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
    public Layer obtainLayer(String layerId, UUID eventId) {
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

    @Override
    public boolean isApplicable(String layerId) {
        return false;
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
                    step.label(), null, null);
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
                    step.label(), null, null);
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
