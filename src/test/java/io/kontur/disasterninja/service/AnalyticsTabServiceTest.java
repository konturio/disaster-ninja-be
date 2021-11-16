package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.AnalyticsTabProperties;
import io.kontur.disasterninja.dto.AnalyticsDto;
import io.kontur.disasterninja.dto.AnalyticsField;
import io.kontur.disasterninja.dto.Function;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsTabServiceTest {

    @Mock
    private InsightsApiGraphqlClient insightsApiGraphqlClient;

    @Mock
    private AnalyticsTabProperties configuration;

    @InjectMocks
    private AnalyticsTabService service;

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

        List<AnalyticsTabQuery.Function> functionsResults = List.of(new AnalyticsTabQuery.Function("", "functionId", 1.1),
                new AnalyticsTabQuery.Function("", "functionId1", 2.233333),
                new AnalyticsTabQuery.Function("", "functionId2", 4.433333));

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = new CompletableFuture();
        completableFuture.complete(functionsResults);

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);
        ArgumentCaptor<List<FunctionArgs>> functionArgsArgumentCaptor = ArgumentCaptor.forClass(List.class);

        when(configuration.getFields()).thenReturn(List.of(analyticsField));
        when(insightsApiGraphqlClient.analyticsTabQuery(geoJSONArgumentCaptor.capture(), functionArgsArgumentCaptor.capture())).thenReturn(completableFuture);

        //when
        List<AnalyticsDto> result = service.calculateAnalytics(geoJsonString);

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
        assertEquals(1, result1.getPercentValue());
        assertEquals("2 people on 4.43 km2 ", result1.getText());
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
            service.calculateAnalytics(geoJsonString);
        } catch (WebApplicationException e) {
            assertEquals("Exception when getting data from insights-api using apollo client", e.getMessage());
        }
    }

    @Test
    public void calculateAnalyticsTestZeroResults(){
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

        List<AnalyticsTabQuery.Function> functionsResults = List.of(new AnalyticsTabQuery.Function("", "functionId", 0.0),
                new AnalyticsTabQuery.Function("", "functionId1", null));

        CompletableFuture<List<AnalyticsTabQuery.Function>> completableFuture = new CompletableFuture();
        completableFuture.complete(functionsResults);

        when(configuration.getFields()).thenReturn(List.of(analyticsField));
        when(insightsApiGraphqlClient.analyticsTabQuery(any(GeoJSON.class), anyList())).thenReturn(completableFuture);

        //when
        List<AnalyticsDto> result = service.calculateAnalytics(geoJsonString);

        //then
        assertEquals(1, result.size());
        AnalyticsDto result1 = result.get(0);
        assertEquals("name", result1.getName());
        assertEquals("description", result1.getDescription());
        assertEquals(0, result1.getPercentValue());
        assertEquals("0 people on ", result1.getText());
    }
}