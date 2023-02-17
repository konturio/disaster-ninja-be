package io.kontur.disasterninja.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.AnalyticsTabProperties;
import io.kontur.disasterninja.dto.*;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import io.kontur.disasterninja.mapper.AnalyticsMapper;
import io.kontur.disasterninja.mapper.BivariateStatisticMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSON;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);
    private final InsightsApiGraphqlClient insightsApiGraphqlClient;
    private final DecimalFormat numberFormat = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.US));
    private final AnalyticsTabProperties configuration;
    private final ApplicationService applicationService;

    private final AnalyticsMapper mapper = Mappers.getMapper(AnalyticsMapper.class);

    private final ObjectMapper objectMapper;

    public List<AnalyticsDto> calculateAnalyticsForPanel(GeoJSON geoJSON) {
        List<AnalyticsField> fields = configuration.getFields();
        List<Function> functions = fields.stream()
                .flatMap(field -> field.getFunctions().stream())
                .toList();
        List<AnalyticsTabQuery.Function> functionsResults = calculateRawAnalytics(geoJSON, functions);

        return createResultDto(fields, functionsResults);
    }

    public List<AnalyticsResponseDto> calculateAnalyticsForPanelUsingAppConfig(AnalyticsRequestDto analyticsRequestDto) {

        List<AnalyticsStatisticsConfigurationDto> configurations = getAnalyticsConfigurationForApplication(analyticsRequestDto.getAppId());

        List<Function> functions = new ArrayList<>();

        for (int i = 0; i < configurations.size(); i++) {

            List<String> arguments = new ArrayList<>();
            arguments.add(configurations.get(i).getX());
            arguments.add(configurations.get(i).getY());

            functions.add(new Function()
                    .setId(Integer.toString(i))
                    .setFunction(configurations.get(i).getFormula())
                    .setArguments(arguments));
        }

        List<AnalyticsTabQuery.Function> functionsResults = calculateRawAnalytics(analyticsRequestDto.getFeatures(), functions);

        return organizeResponse(functions, functionsResults);
    }

    private List<AnalyticsResponseDto> organizeResponse(List<Function> functions, List<AnalyticsTabQuery.Function> functionsResults) {
        List<AnalyticsResponseDto> analytics = new ArrayList<>();
        for (AnalyticsTabQuery.Function functionResult : functionsResults) {

            Function function = functions.stream().filter(f -> f.getId().equals(functionResult.id())).findFirst().orElseThrow();

            AnalyticsResponseDto combinedData = new AnalyticsResponseDto();

            switch (function.getFunction()) {
                case "maxX" -> combinedData.setPrefix("Maximum");
                case "sumX" -> combinedData.setPrefix("Total");
                case "sumXWhereNoY" -> combinedData.setPrefix("Total with no");
                case "percentageXWhereNoY" -> combinedData.setPrefix("Percent with no");
                default -> throw new WebApplicationException("Not a proper function name found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            analytics.add(combinedData
                    .setFormula(function.getFunction())
                    .setValue(functionResult.result())
                    .setXLabel(functionResult.x_label())
                    .setYLabel(functionResult.y_label())
                    .setUnit(mapper.analyticsTabQueryUnitToUnit(functionResult.unit())));
        }
        return analytics;
    }

    private List<AnalyticsStatisticsConfigurationDto> getAnalyticsConfigurationForApplication(UUID appUuid) {
        try {
            return Arrays.asList(objectMapper.treeToValue(applicationService.getAppConfig(appUuid)
                    .getFeatures()
                    .stream()
                    .map(FeatureDto::getConfiguration)
                    .filter(c -> c != null && c.get("statistics") != null)
                    .findFirst().orElseThrow()
                    .get("statistics"), AnalyticsStatisticsConfigurationDto[].class));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new WebApplicationException(
                    String.format("Can't find or convert configuration for analytics feature for application with UUID = %s", appUuid),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AnalyticsTabQuery.Function> calculateRawAnalytics(GeoJSON geoJSON, List<Function> functions) {
        List<FunctionArgs> functionArgs = createFunctionArgsList(functions);
        try {
            return insightsApiGraphqlClient.analyticsTabQuery(geoJSON, functionArgs).get();
        } catch (Exception e) {
            LOG.error("Can't load analytics data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    private List<FunctionArgs> createFunctionArgsList(List<Function> functions) {
        return functions.stream()
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

    private List<AnalyticsDto> createResultDto(List<AnalyticsField> fields,
                                               List<AnalyticsTabQuery.Function> functionsResults) {
        List<AnalyticsDto> result = new ArrayList<>();

        Map<String, Double> functionsResultsMap = functionsResults.stream()
                .collect(Collectors.toMap(AnalyticsTabQuery.Function::id,
                        value -> Optional.ofNullable(value.result()).orElse(0.0)));

        fields.forEach(field -> {
            AnalyticsDto dto = new AnalyticsDto();
            StringBuilder text = new StringBuilder();
            List<Function> currentFunctionsList = field.getFunctions();
            for (Function function : currentFunctionsList) {
                if (functionsResultsMap.get(function.getId()) == null) {
                    continue;
                }
                switch (function.getPostfix()) {
                    case ("%") -> dto.setPercentValue(BigDecimal.valueOf(functionsResultsMap.get(function.getId()))
                            .setScale(0, RoundingMode.UP).intValue());
                    case ("people on") -> text.append(numberFormat.format(
                                    BigDecimal.valueOf(functionsResultsMap.get(function.getId()))
                                            .setScale(0, RoundingMode.UP).longValue()))
                            .append(" ")
                            .append(function.getPostfix())
                            .append(" ");
                    default -> text.append(numberFormat.format(
                                    BigDecimal.valueOf(functionsResultsMap.get(function.getId()))
                                            .setScale(2, RoundingMode.HALF_UP).doubleValue()))
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
