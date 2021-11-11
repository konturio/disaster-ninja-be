package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.AnalyticsTabProperties;
import io.kontur.disasterninja.dto.AnalyticsDto;
import io.kontur.disasterninja.dto.AnalyticsField;
import io.kontur.disasterninja.dto.Function;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSONFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsTabService {

    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

    private final AnalyticsTabProperties configuration;

    public List<AnalyticsDto> calculateAnalytics(String geoJsonString) {
        List<AnalyticsField> fields = configuration.getFields();
        List<AnalyticsTabQuery.Function> functionsResults = null;
        try {
            functionsResults = insightsApiGraphqlClient.analyticsTabQuery(GeoJSONFactory.create(geoJsonString),
                    createFunctionArgsList(fields)).get();
        } catch (InterruptedException | ExecutionException | WebApplicationException e) {
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client", HttpStatus.BAD_GATEWAY);
        }
        return createResultDto(fields, functionsResults);
    }

    private List<FunctionArgs> createFunctionArgsList(List<AnalyticsField> fields) {
        return fields.stream()
                .flatMap(field -> field.getFunctions().stream())
                .map(field -> FunctionArgs.builder()
                        .id(field.getId())
                        .name(field.getFunction())
                        .x(getArgumentIfExists(field.getArguments(), 0))
                        .y(getArgumentIfExists(field.getArguments(), 1))
                        .build())
                .toList();
    }

    private String getArgumentIfExists(List<String> arguments, int index) {
        if (index < arguments.size()) {
            return arguments.get(index);
        }
        return null;
    }

    private List<AnalyticsDto> createResultDto(List<AnalyticsField> fields, List<AnalyticsTabQuery.Function> functionsResults) {
        List<AnalyticsDto> result = new ArrayList<>();

        Map<String, Double> functionsResultsMap = functionsResults.stream()
                .collect(Collectors.toMap(AnalyticsTabQuery.Function::id,
                        value -> Optional.ofNullable(value.result()).orElse(0.0)));

        fields.forEach(field -> {
            AnalyticsDto dto = new AnalyticsDto();
            StringBuilder text = new StringBuilder();
            List<Function> currentFunctionsList = field.getFunctions();
            for (Function function : currentFunctionsList) {
                if (function.getPostfix().equals("%")) {
                    dto.setPercentValue(BigDecimal.valueOf(functionsResultsMap.get(function.getId()))
                            .setScale(0, RoundingMode.HALF_UP).intValue());
                } else {
                    text.append(BigDecimal.valueOf(functionsResultsMap.get(function.getId()))
                            .setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .append(" ")
                            .append(function.getPostfix())
                            .append(" ");
                }
            }
            dto.setName(field.getName());
            dto.setDescription(field.getDescription());
            dto.setText(text.toString());
            result.add(dto);
        });
        return result;
    }
}
