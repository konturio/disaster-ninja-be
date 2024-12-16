package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.domain.*;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateStatisticDto;
import io.kontur.disasterninja.dto.bivariatematrix.IndicatorDto;
import io.kontur.disasterninja.dto.bivariatematrix.OverlayDto;
import io.kontur.disasterninja.dto.layer.ColorDto;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.enums.LayerType.VECTOR;
import static io.kontur.disasterninja.domain.enums.LegendType.BIVARIATE;
import static java.util.Objects.requireNonNull;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
public class BivariateLayerProvider implements LayerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BivariateLayerProvider.class);
    public static final String LAYER_PREFIX = "BIV__";
    private static final String MARKDOWN_LINK_PATTERN = "[%s](%s)";
    private static final Pattern URL_SEARCH_PATTERN = Pattern.compile(
            "(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])");
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;
    private volatile Map<String, Layer> bivariateLayers = new ConcurrentHashMap<>();

    @Value("${kontur.platform.insightsApi.useInsightsV2:false}")
    private Boolean useInsightsV2;

    @PostConstruct
    @Scheduled(initialDelayString = "${kontur.platform.insightsApi.layersReloadInterval}",
            fixedRateString = "${kontur.platform.insightsApi.layersReloadInterval}")
    public void reloadLayers() {
        LOG.info("Loading bivariate layers from insights-api");
        try {
            BivariateStatisticDto bivariateStatisticDto = insightsApiGraphqlClient.getBivariateStatistic().get();
            if (bivariateStatisticDto != null && bivariateStatisticDto.getOverlays() != null) {
                synchronized (this) {
                    bivariateLayers = bivariateStatisticDto.getOverlays().stream()
                            .map(overlay -> fromOverlay(overlay, bivariateStatisticDto.getIndicators()))
                            .collect(Collectors.toMap(Layer::getId, it -> it));
                }
                LOG.info("Loaded bivariate layers: {}", String.join(", ", bivariateLayers.keySet()));
            } else {
                LOG.error("Can't load list of available bivariate layers: no overlays received");
            }
        } catch (Exception e) {
            LOG.error("Can't load list of available bivariate layers: {}", e.getMessage(), e);
        }
    }

    @Override
    @Timed(value = "layers.obtainGlobalLayers", histogram = true)
    public CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams) {
        reloadLayersIfEmpty();
        return CompletableFuture.completedFuture(bivariateLayers.values().stream().toList());
    }

    @Override
    public CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
    }

    /**
     * @return Bivariate layer by ID from insights-api graphql api
     */
    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        reloadLayersIfEmpty();
        if (!bivariateLayers.containsKey(layerId)) {
            reloadLayers();
        }
        return bivariateLayers.get(layerId);
    }

    private void reloadLayersIfEmpty() {
        if (bivariateLayers.isEmpty()) {
            synchronized (this) {
                if (bivariateLayers.isEmpty()) {
                    reloadLayers();
                }
            }
        }
    }

    protected Layer fromOverlay(OverlayDto overlay, List<IndicatorDto> indicators) {
        if (overlay == null) {
            return null;
        }
        Legend legend = bivariateLegendFromOverlay(overlay);

        List<String> copyrights = copyrightsFromIndicators(legend, indicators);

        String url = (useInsightsV2 != null && useInsightsV2)
                ? "api/tiles/bivariate/v2/{z}/{x}/{y}.mvt?indicatorsClass=general"
                : "api/tiles/bivariate/v1/{z}/{x}/{y}.mvt?indicatorsClass=general";
        return Layer.builder()
                .id(getIdWithPrefix(overlay.getName()))
                .name(overlay.getName())
                .description(overlay.getDescription())
                .source(LayerSource.builder()
                        .type(VECTOR)
                        .urls(List.of(url))
                        .tileSize(512)
                        .build())
                .legend(legend)
                .copyrights(copyrights)
//                .maxZoom(8)
                .globalOverlay(true)
                .boundaryRequiredForRetrieval(false)
                .eventIdRequiredForRetrieval(false)
                .displayLegendIfNoFeaturesExist(true)
                .category(LayerCategory.OVERLAY)
                .group("bivariate")
                .orderIndex(overlay.getOrder())
                .build();
    }

    private String getIdWithPrefix(String id) {
        return LAYER_PREFIX + id;
    }

    private Legend bivariateLegendFromOverlay(OverlayDto overlay) {
        if (overlay == null) {
            return null;
        }

        //AXIS 1
        BivariateLegendAxisDescription xAxis = new BivariateLegendAxisDescription();
        BivariateLegendAxisDescription x = overlay.getX();
        if (x != null) {
            if (x.getSteps() != null) {
                List<BivariateLegendAxisStep> steps = requireNonNull(x.getSteps()).stream()
                        .map(step -> BivariateLegendAxisStep.builder()
                                .value(step.getValue())
                                .label(step.getLabel())
                                .build())
                        .toList();
                xAxis.setSteps(steps);
            }
            xAxis.setLabel(x.getLabel());
            xAxis.setQuotient(x.getQuotient());

            if (x.getQuotients() != null) {
                List<BivariateLegendQuotient> quotients = requireNonNull(x.getQuotients()).stream()
                        .map(quotient -> BivariateLegendQuotient.builder()
                                .name(quotient.getName())
                                .label(quotient.getLabel())
                                .maxZoom(quotient.getMaxZoom())
                                .direction(quotient.getDirection())
                                .unit(quotient.getUnit())
                                .build())
                        .toList();
                xAxis.setQuotients(quotients);
            }
        }
        //AXIS 2
        BivariateLegendAxisDescription yAxis = new BivariateLegendAxisDescription();
        BivariateLegendAxisDescription y = overlay.getY();
        if (y != null) {
            if (y.getSteps() != null) {
                List<BivariateLegendAxisStep> steps = requireNonNull(y.getSteps()).stream()
                        .map(step1 -> BivariateLegendAxisStep.builder()
                                .value(step1.getValue())
                                .label(step1.getLabel())
                                .build()).toList();
                yAxis.setSteps(steps);
            }

            yAxis.setLabel(y.getLabel());
            yAxis.setQuotient(y.getQuotient());

            if (y.getQuotients() != null) {
                List<BivariateLegendQuotient> quotients = requireNonNull(y.getQuotients()).stream()
                        .map(quotient -> BivariateLegendQuotient.builder()
                                .name(quotient.getName())
                                .label(quotient.getLabel())
                                .maxZoom(quotient.getMaxZoom())
                                .direction(quotient.getDirection())
                                .unit(quotient.getUnit())
                                .build())
                        .toList();
                yAxis.setQuotients(quotients);
            }
        }
        //colors matrix
        List<ColorDto> colors = requireNonNull(overlay.getColors()).stream()
                .map(c -> new ColorDto(c.getId(), c.getColor()))
                .collect(Collectors.toList());

        return Legend.builder()
                .type(BIVARIATE)
                .colors(colors)
                .axes(BivariateLegendAxes.builder()
                        .x(xAxis)
                        .y(yAxis)
                        .build())
                .build();
    }

    private List<String> copyrightsFromIndicators(Legend legend, List<IndicatorDto> indicators) {
        Set<BivariateLegendQuotient> quotients = new HashSet<>();
        quotients.addAll(legend.getAxes().getX().getQuotients());
        quotients.addAll(legend.getAxes().getY().getQuotients());

        Set<String> copyrights = new HashSet<>();
        quotients.forEach(q ->
                indicators.stream()
                        .filter(indicator -> Objects.equals(indicator.getName(), q.getName()))
                        .findFirst()
                        .map(IndicatorDto::getCopyrights)
                        .orElseGet(ArrayList::new)
                        .stream()
                        .map(str -> URL_SEARCH_PATTERN.matcher(str)
                                .replaceAll(matchResult ->
                                        String.format(MARKDOWN_LINK_PATTERN, matchResult.group(0),
                                                matchResult.group(0))))
                        .forEach(copyrights::add)
        );
        return copyrights.stream().toList();
    }
}
