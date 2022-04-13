package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.AdvancedAnalyticsDto;
import io.kontur.disasterninja.dto.AdvancedAnalyticsRequestDto;
import io.kontur.disasterninja.dto.AdvancedAnalyticsRequestValuesDto;
import io.kontur.disasterninja.dto.AdvancedAnalyticsValuesDto;
import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import io.kontur.disasterninja.graphql.type.AdvancedAnalyticsRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsPanelService {
    private static final Logger LOG = LoggerFactory.getLogger(AdvancedAnalyticsPanelService.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

    public List<AdvancedAnalyticsDto> calculateAnalytics(AdvancedAnalyticsRequestDto argAdvancedAnalyticsRequest) {
        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> analyticsResult;
        List<AdvancedAnalyticsRequest> request = createRequest(argAdvancedAnalyticsRequest);
        try {
            analyticsResult = insightsApiGraphqlClient.advancedAnalyticsPanelQuery(argAdvancedAnalyticsRequest.getFeatures(), request).get();
        } catch (Exception e) {
            LOG.error("Can't load advanced analytics data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
        return createResultDto(analyticsResult);
    }

    private List<AdvancedAnalyticsRequest> createRequest(AdvancedAnalyticsRequestDto argAdvancedAnalyticsRequest) {
        List<AdvancedAnalyticsRequestValuesDto> values = argAdvancedAnalyticsRequest.getValues();
        List<AdvancedAnalyticsRequest> returnList = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            values.stream().map(r -> returnList.add(AdvancedAnalyticsRequest.builder().numerator(r.getNumerator()).denominator(r.getDenominator()).calculations(r.getCalculations()).build())).toList();
            return returnList;
        } else {
            return null;
        }
    }

    private List<AdvancedAnalyticsDto> createResultDto(List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> argAnalyticsResult) {
        return argAnalyticsResult.stream().map(advAnalytics -> {
            List<AdvancedAnalyticsValuesDto> valueList = advAnalytics.analytics().stream()
                    .map(values -> new AdvancedAnalyticsValuesDto(values.calculation(), values.value(), values.quality())).toList();
            return new AdvancedAnalyticsDto(advAnalytics.numerator(), advAnalytics.denominator(), advAnalytics.numeratorLabel(), advAnalytics.denominatorLabel(), valueList);
        }).toList();
    }
}
