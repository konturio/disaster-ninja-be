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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private InsightsApiGraphqlClient insightsApiGraphqlClient;

    @Mock
    private AnalyticsTabProperties configuration;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ObjectMapper objectMapperMock;

    @InjectMocks
    private AnalyticsService service;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void calculateAnalyticsTest() {
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        AnalyticsField analyticsField = new AnalyticsField();
        Function function = new Function();
        function.setId("functionId");
        function.setPostfix("%");
        function.setFunction("percentageXWhereNoY");
        function.setArguments(List.of("populated_area_km2", "count"));

        Function function1 = new Function();
        function1.setId("functionId1");
        function1.setPostfix("people on");
        function1.setFunction("sumXWhereNoY");
        function1.setArguments(List.of("population", "count"));

        Function function2 = new Function();
        function2.setId("functionId2");
        function2.setPostfix("km2");
        function2.setFunction("sumXWhereNoY");
        function2.setArguments(List.of("populated_area_km2", "count"));

        analyticsField.setFunctions(List.of(function, function1, function2));
        analyticsField.setDescription("description");
        analyticsField.setName("name");

        List<AnalyticsTabQuery.Function> functionsResults = List.of(
                new AnalyticsTabQuery.Function("", "functionId", 1.1, new AnalyticsTabQuery.Unit(
                        "", "km2", "km²", "square kilometers"), "Populated area", "OSM: objects count"),
                new AnalyticsTabQuery.Function("", "functionId1", 11442.233333, new AnalyticsTabQuery.Unit(
                        "", "ppl", "ppl", "people"), "Population", "OSM: objects count"),
                new AnalyticsTabQuery.Function("", "functionId2", 1444.433333, new AnalyticsTabQuery.Unit(
                        "", "km2", "km²", "square kilometers"), "Populated area", "OSM: objects count"));

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = new CompletableFuture();
        completableFuture.complete(functionsResults);

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);
        ArgumentCaptor<List<FunctionArgs>> functionArgsArgumentCaptor = ArgumentCaptor.forClass(List.class);

        when(configuration.getFields()).thenReturn(List.of(analyticsField));
        when(insightsApiGraphqlClient.analyticsTabQuery(geoJSONArgumentCaptor.capture(),
                functionArgsArgumentCaptor.capture())).thenReturn(completableFuture);

        //when
        List<AnalyticsDto> result = service.calculateAnalyticsForPanel(GeoJSONFactory.create(geoJsonString));

        //then
        GeoJSON geoJSONCaptorValue = geoJSONArgumentCaptor.getValue();
        assertEquals(GeoJSONFactory.create(geoJsonString).toString(), geoJSONCaptorValue.toString());

        List<FunctionArgs> functionArgsCaptorValue = functionArgsArgumentCaptor.getValue();
        assertEquals(3, functionArgsCaptorValue.size());

        FunctionArgs functionArgs = functionArgsCaptorValue.get(0);
        assertEquals("functionId", functionArgs.id());
        assertEquals("percentageXWhereNoY", functionArgs.name());
        assertEquals("populated_area_km2", functionArgs.x());
        assertEquals("count", functionArgs.y());

        functionArgs = functionArgsCaptorValue.get(1);
        assertEquals("functionId1", functionArgs.id());
        assertEquals("sumXWhereNoY", functionArgs.name());
        assertEquals("population", functionArgs.x());
        assertEquals("count", functionArgs.y());

        assertEquals(1, result.size());
        AnalyticsDto result1 = result.get(0);
        assertEquals("name", result1.getName());
        assertEquals("description", result1.getDescription());
        assertEquals(2, result1.getPercentValue());
        assertEquals("11,443 people on 1,444.43 km2 ", result1.getText());
    }

    @Test
    public void calculateAnalyticsUsingAppConfigTest() throws JsonProcessingException {
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        UUID applicationId = UUID.randomUUID();

        List<AnalyticsTabQuery.Function> functionsResults = List.of(
                new AnalyticsTabQuery.Function("", "0", 1.1, new AnalyticsTabQuery.Unit(
                        "", "km2", "km²", "square kilometers"), "Populated area", "OSM: objects count"),
                new AnalyticsTabQuery.Function("", "1", 11442.233333, new AnalyticsTabQuery.Unit(
                        "", "ppl", "ppl", "people"), "Population", "OSM: objects count"),
                new AnalyticsTabQuery.Function("", "2", 1444.433333, new AnalyticsTabQuery.Unit(
                        "", "ppl", "ppl", "people"), "Population", null));

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = new CompletableFuture();
        completableFuture.complete(functionsResults);

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);
        ArgumentCaptor<List<FunctionArgs>> functionArgsArgumentCaptor = ArgumentCaptor.forClass(List.class);


        JsonNode configuration = objectMapper.readValue("{\"statistics\": [{\"x\": \"populated_area_km2\", \"y\": \"count\", \"formula\": \"percentageXWhereNoY\"}, {\"x\": \"population\", \"y\": \"count\", \"formula\": \"sumXWhereNoY\"}, {\"x\": \"population\", \"formula\": \"sumX\"}]}", JsonNode.class);
        FeatureDto feature = new FeatureDto("analytics_panel", "Analytics panel", FeatureDto.FeatureType.UI_PANEL);
        feature.setConfiguration(configuration);
        AppDto appDto = new AppDto();
        appDto.setFeatures(List.of(feature));

        AnalyticsStatisticsConfigurationDto[] array = new AnalyticsStatisticsConfigurationDto[3];
        array[0] = new AnalyticsStatisticsConfigurationDto("percentageXWhereNoY", "populated_area_km2", "count");
        array[1] = new AnalyticsStatisticsConfigurationDto("sumXWhereNoY", "population", "count");
        array[2] = new AnalyticsStatisticsConfigurationDto("sumX", "population", null);

        when(applicationService.getAppConfig(any(UUID.class))).thenReturn(appDto);
        when(objectMapperMock.treeToValue(any(JsonNode.class), same(AnalyticsStatisticsConfigurationDto[].class))).thenReturn(array);
        when(insightsApiGraphqlClient.analyticsTabQuery(geoJSONArgumentCaptor.capture(),
                functionArgsArgumentCaptor.capture())).thenReturn(completableFuture);

        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setAppId(applicationId);
        request.setFeatures(GeoJSONFactory.create(geoJsonString));

        //when
        List<AnalyticsResponseDto> result = service.calculateAnalyticsForPanelUsingAppConfig(request);

        //then
        GeoJSON geoJSONCaptorValue = geoJSONArgumentCaptor.getValue();
        assertEquals(GeoJSONFactory.create(geoJsonString).toString(), geoJSONCaptorValue.toString());

        List<FunctionArgs> functionArgsCaptorValue = functionArgsArgumentCaptor.getValue();
        assertEquals(3, functionArgsCaptorValue.size());

        FunctionArgs functionArgs = functionArgsCaptorValue.get(0);
        assertEquals("0", functionArgs.id());
        assertEquals("percentageXWhereNoY", functionArgs.name());
        assertEquals("populated_area_km2", functionArgs.x());
        assertEquals("count", functionArgs.y());

        functionArgs = functionArgsCaptorValue.get(1);
        assertEquals("1", functionArgs.id());
        assertEquals("sumXWhereNoY", functionArgs.name());
        assertEquals("population", functionArgs.x());
        assertEquals("count", functionArgs.y());

        assertEquals(3, result.size());
        AnalyticsResponseDto result0 = result.get(0);
        assertEquals("percentageXWhereNoY", result0.getFormula());
        assertEquals(1.1, result0.getValue());
        assertEquals("Percent with no", result0.getPrefix());
        assertEquals("Populated area", result0.getXLabel());
        assertEquals("OSM: objects count", result0.getYLabel());
        assertEquals("km2", result0.getUnit().getId());
    }

    @Test
    public void calculateAnalyticsUsingAppConfigTestException() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        UUID applicationId = UUID.randomUUID();

        AnalyticsRequestDto request = new AnalyticsRequestDto();
        request.setAppId(applicationId);
        request.setFeatures(GeoJSONFactory.create(geoJsonString));

        JsonNode configuration = objectMapper.readValue("{\"statistics\": [{\"x\": \"populated_area_km2\", \"y\": \"count\", \"formula\": \"percentageXWhereNoY\"}, {\"x\": \"population\", \"y\": \"count\", \"formula\": \"sumXWhereNoY\"}, {\"x\": \"population\", \"formula\": \"sumX\"}]}", JsonNode.class);
        FeatureDto feature = new FeatureDto("analytics_panel", "Analytics panel", FeatureDto.FeatureType.UI_PANEL);
        feature.setConfiguration(configuration);
        AppDto appDto = new AppDto();
        appDto.setFeatures(List.of(feature));

        AnalyticsStatisticsConfigurationDto[] array = new AnalyticsStatisticsConfigurationDto[3];
        array[0] = new AnalyticsStatisticsConfigurationDto("percentageXWhereNoY", "populated_area_km2", "count");
        array[1] = new AnalyticsStatisticsConfigurationDto("sumXWhereNoY", "population", "count");
        array[2] = new AnalyticsStatisticsConfigurationDto("sumX", "population", null);

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = mock(CompletableFuture.class);

        when(applicationService.getAppConfig(any(UUID.class))).thenReturn(appDto);
        when(objectMapperMock.treeToValue(any(JsonNode.class), same(AnalyticsStatisticsConfigurationDto[].class))).thenReturn(array);
        when(insightsApiGraphqlClient.analyticsTabQuery(any(GeoJSON.class), anyList())).thenReturn(completableFuture);

        when(completableFuture.get()).thenThrow(new InterruptedException());

        //when
        try {
            service.calculateAnalyticsForPanelUsingAppConfig(request);
            throw new RuntimeException("expected exception was not thrown");
        } catch (WebApplicationException e) {
            assertEquals("Exception when getting data from insights-api using apollo client", e.getMessage());
        }
    }

    @Test
    public void calculateAnalyticsTestException() throws ExecutionException, InterruptedException {
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        AnalyticsField analyticsField = new AnalyticsField();
        Function function = new Function();
        function.setId("functionId");
        function.setPostfix("%");
        function.setFunction("percentageXWhereNoY");
        function.setArguments(List.of("populated_area_km2", "count"));

        Function function1 = new Function();
        function1.setId("functionId1");
        function1.setPostfix("people on");
        function1.setFunction("sumXWhereNoY");
        function1.setArguments(List.of("population", "count"));

        analyticsField.setFunctions(List.of(function, function1));
        analyticsField.setDescription("description");
        analyticsField.setName("name");

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = mock(CompletableFuture.class);

        when(configuration.getFields()).thenReturn(List.of(analyticsField));
        when(insightsApiGraphqlClient.analyticsTabQuery(any(GeoJSON.class), anyList())).thenReturn(completableFuture);
        when(completableFuture.get()).thenThrow(new InterruptedException());

        //when
        try {
            service.calculateAnalyticsForPanel(GeoJSONFactory.create(geoJsonString));
            throw new RuntimeException("expected exception was not thrown");
        } catch (WebApplicationException e) {
            assertEquals("Exception when getting data from insights-api using apollo client", e.getMessage());
        }
    }

    @Test
    public void calculateAnalyticsTestZeroResults() {
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        AnalyticsField analyticsField = new AnalyticsField();
        Function function = new Function();
        function.setId("functionId");
        function.setPostfix("%");
        function.setFunction("percentageXWhereNoY");
        function.setArguments(List.of("populated_area_km2", "count"));

        Function function1 = new Function();
        function1.setId("functionId1");
        function1.setPostfix("people on");
        function1.setFunction("sumXWhereNoY");
        function1.setArguments(List.of("population", "count"));

        analyticsField.setFunctions(List.of(function, function1));
        analyticsField.setDescription("description");
        analyticsField.setName("name");

        List<AnalyticsTabQuery.Function> functionsResults = List.of(
                new AnalyticsTabQuery.Function("", "functionId", 0.0, new AnalyticsTabQuery.Unit(
                        "", "km2", "km²", "square kilometers"), "Populated area", "OSM: objects count"),
                new AnalyticsTabQuery.Function("", "functionId1", null, new AnalyticsTabQuery.Unit(
                        "", "ppl", "ppl", "people"), "Population", "OSM: objects count"));

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = new CompletableFuture();
        completableFuture.complete(functionsResults);

        when(configuration.getFields()).thenReturn(List.of(analyticsField));
        when(insightsApiGraphqlClient.analyticsTabQuery(any(GeoJSON.class), anyList())).thenReturn(completableFuture);

        //when
        List<AnalyticsDto> result = service.calculateAnalyticsForPanel(GeoJSONFactory.create(geoJsonString));

        //then
        assertEquals(1, result.size());
        AnalyticsDto result1 = result.get(0);
        assertEquals("name", result1.getName());
        assertEquals("description", result1.getDescription());
        assertEquals(0, result1.getPercentValue());
        assertEquals("0 people on ", result1.getText());
    }
}