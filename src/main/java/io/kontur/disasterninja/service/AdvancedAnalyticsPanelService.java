package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.AdvancedAnalyticsDto;
import io.kontur.disasterninja.dto.AdvancedAnalyticsValuesDto;
import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsPanelService {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedAnalyticsPanelService.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

    public List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> calculateAnalytics(GeoJSON geoJSON) {
        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> analyticsResult;
        try {
            analyticsResult = insightsApiGraphqlClient.advancedAnalyticsPanelQuery(geoJSON).get();
        } catch (Exception e) {
            LOG.error("Can't load advanced analytics data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                HttpStatus.BAD_GATEWAY);
        }
        // WHAT YOU THINK SHOULD I CONVERT List<AdvancedAnalyticsTabQuery.AdvancedAnalytic> to List<AdvancedAnalyticsDto>
        // THERE MAY BE SOME ADDITIONAL CALCULATIONS, BUT NOT FOR NOW
        return analyticsResult;
    }

    private List<AdvancedAnalyticsDto> createResultDto(List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> argAnalyticsResult) {
        return argAnalyticsResult.stream().map(new Function<AdvancedAnalyticalPanelQuery.AdvancedAnalytic, AdvancedAnalyticsDto>() {
            @Override
            public AdvancedAnalyticsDto apply(AdvancedAnalyticalPanelQuery.AdvancedAnalytic advAnalytics) {
                List<AdvancedAnalyticsValuesDto> valueList = advAnalytics.analytics().stream()
                        .map(new Function<AdvancedAnalyticalPanelQuery.Analytic, AdvancedAnalyticsValuesDto>() {
                    @Override
                    public AdvancedAnalyticsValuesDto apply(AdvancedAnalyticalPanelQuery.Analytic values) {
                        return new AdvancedAnalyticsValuesDto(values.calculation(), values.value(), values.quality());
                    }
                }).collect(Collectors.toList());

                return new AdvancedAnalyticsDto(advAnalytics.numerator(), advAnalytics.denominator(), advAnalytics.numeratorLabel(), advAnalytics.denominatorLabel(), valueList);
            }
        }).collect(Collectors.toList());
    }
}
