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
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsPanelService {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedAnalyticsPanelService.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

    public List<AdvancedAnalyticsDto> calculateAnalytics(FeatureCollection geoJSON) {
        if(geoJSON != null && geoJSON.getFeatures() != null && geoJSON.getFeatures().length == 0)
        {
            geoJSON = null;
        }
        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> analyticsResult;
        try {
            analyticsResult = insightsApiGraphqlClient.advancedAnalyticsPanelQuery(geoJSON).get();
        } catch (Exception e) {
            LOG.error("Can't load advanced analytics data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
        return createResultDto(analyticsResult);
    }

    private List<AdvancedAnalyticsDto> createResultDto(List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> argAnalyticsResult) {
        return argAnalyticsResult.stream().map(advAnalytics -> {
            List<AdvancedAnalyticsValuesDto> valueList = advAnalytics.analytics().stream()
                    .map(values -> new AdvancedAnalyticsValuesDto(values.calculation(), values.value(), values.quality())).collect(Collectors.toList());
            return new AdvancedAnalyticsDto(advAnalytics.numerator(), advAnalytics.denominator(), advAnalytics.numeratorLabel(), advAnalytics.denominatorLabel(), valueList);
        }).collect(Collectors.toList());
    }
}
