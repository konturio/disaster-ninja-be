package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.*;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.dto.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
import static io.kontur.disasterninja.domain.enums.LegendType.BIVARIATE;
import static java.util.Objects.requireNonNull;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class BivariateLayerProvider implements LayerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BivariateLayerProvider.class);
    private static final String MARKDOWN_LINK_PATTERN = "[%s](%s)";
    private static final Pattern URL_SEARCH_PATTERN = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])");
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;
    @Value("${kontur.platform.tiles.host}")
    private String tilesHost;
    private List<String> bivariateLayerIds = List.of();

    @PostConstruct
    private void init() {
        try {
            BivariateStatisticDto bivariateStatisticDto = insightsApiGraphqlClient.getBivariateStatistic().get();
            if (bivariateStatisticDto != null && bivariateStatisticDto.getOverlays() != null) {
                bivariateLayerIds = bivariateStatisticDto.getOverlays().stream()
                    .map(BivariateLayerLegendQuery.Overlay::name)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOG.warn("Can't load list of available bivariate layers: {}", e.getMessage(), e);
        }
    }

    /**
     * @return Bivariate layers from insights-api graphql api
     */
    @Override
    public List<Layer> obtainLayers(LayerSearchParams searchParams) {
        try {
            BivariateStatisticDto bivariateStatisticDto = insightsApiGraphqlClient.getBivariateStatistic().get();
            return bivariateStatisticDto.getOverlays().stream()
                .map(overlay -> fromOverlay(overlay, bivariateStatisticDto.getIndicators()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Can't load bivariate layers: {}", e.getMessage(), e);
            throw new WebApplicationException("Can't load bivariate layers", HttpStatus.BAD_GATEWAY);
        }
    }

    @Override
    public boolean isApplicable(String layerId) {
        if (bivariateLayerIds.isEmpty()) {
            init();
        }
        return bivariateLayerIds.contains(layerId);
    }

    protected Layer fromOverlay(BivariateLayerLegendQuery.Overlay overlay, List<BivariateLayerLegendQuery.Indicator> indicators) {
        if (overlay == null) {
            return null;
        }
        Legend legend = bivariateLegendFromOverlay(overlay);

        List<String> copyrights = copyrightsFromIndicators(legend, indicators);

        return Layer.builder()
            .id(overlay.name())
            .name(overlay.name())
            .description(overlay.description())
            .source(LayerSource.builder()
                .type(VECTOR)
                .urls(List.of(tilesHost + "/tiles/stats/{z}/{x}/{y}.mvt"))
                .build())
            .legend(legend)
            .copyrights(copyrights)
            .maxZoom(8)
            .globalOverlay(true)
            .boundaryRequiredForRetrieval(false)
            .eventIdRequiredForRetrieval(false)
            .displayLegendIfNoFeaturesExist(true)
            .category(LayerCategory.OVERLAY)
            .group("bivariate")
            .orderIndex(overlay.order())
            .build();
    }

    /**
     * @return Bivariate layer by ID from insights-api graphql api
     */
    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        try {
            BivariateStatisticDto bivariateStatisticDto = insightsApiGraphqlClient.getBivariateStatistic().get();
            return bivariateStatisticDto.getOverlays().stream()
                    .filter(it -> layerId.equals(it.name())).findFirst()
                    .map(overlay -> fromOverlay(overlay, bivariateStatisticDto.getIndicators()))
                    .orElseGet(() -> null);
        } catch (Exception e) {
            LOG.error("Can't load bivariate layer by id {}: {}",
                layerId, e.getMessage(), e);
            throw new WebApplicationException("Can't load bivariate layer", HttpStatus.BAD_GATEWAY);
        }
    }

    private Legend bivariateLegendFromOverlay(BivariateLayerLegendQuery.Overlay overlay) {
        if (overlay == null) {
            return null;
        }

        //AXIS 1
        BivariateLegendAxisDescription xAxis = new BivariateLegendAxisDescription();
        BivariateLayerLegendQuery.X x = overlay.x();
        if (x != null) {
            if (x.steps() != null) {
                List<BivariateLegendAxisStep> steps = requireNonNull(x.steps()).stream()
                        .map(step -> BivariateLegendAxisStep.builder()
                                .value(step.value())
                                .label(step.label())
                                .build())
                        .toList();
                xAxis.setSteps(steps);
            }
            xAxis.setLabel(x.label());
            xAxis.setQuotient(x.quotient());
        }
        //AXIS 2
        BivariateLegendAxisDescription yAxis = new BivariateLegendAxisDescription();
        BivariateLayerLegendQuery.Y y = overlay.y();
        if (y != null) {
            if (y.steps() != null) {
                List<BivariateLegendAxisStep> steps = requireNonNull(y.steps()).stream()
                        .map(step1 -> BivariateLegendAxisStep.builder()
                                .value(step1.value())
                                .label(step1.label())
                                .build()).toList();
                yAxis.setSteps(steps);
            }
            yAxis.setLabel(y.label());
            yAxis.setQuotient(y.quotient());
        }
        //colors matrix
        Map<String, String> colors = requireNonNull(overlay.colors()).stream()
                .collect(Collectors.toMap(BivariateLayerLegendQuery.Color::id,
                        c -> requireNonNull(c.color())));

        return Legend.builder()
                .type(BIVARIATE)
                .bivariateColors(colors)
                .bivariateAxes(BivariateLegendAxes.builder()
                        .x(xAxis)
                        .y(yAxis)
                        .build())
                .build();
    }

    private List<String> copyrightsFromIndicators(Legend legend, List<BivariateLayerLegendQuery.Indicator> indicators) {
        Set<String> quotient = new HashSet<>();
        quotient.addAll(legend.getBivariateAxes().getX().getQuotient());
        quotient.addAll(legend.getBivariateAxes().getY().getQuotient());

        Set<String> copyrights = new HashSet<>();
        quotient.forEach(q ->
                indicators.stream()
                        .filter(indicator -> Objects.equals(indicator.name(), q))
                        .findFirst()
                        .map(BivariateLayerLegendQuery.Indicator::copyrights)
                        .orElseGet(ArrayList::new)
                        .stream()
                        .map(str -> URL_SEARCH_PATTERN.matcher(str)
                                .replaceAll(matchResult ->
                                        String.format(MARKDOWN_LINK_PATTERN, matchResult.group(0), matchResult.group(0))))
                        .forEach(copyrights::add)
        );
        return copyrights.stream().toList();
    }
}
