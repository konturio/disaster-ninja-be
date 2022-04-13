package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.*;
import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import io.kontur.disasterninja.graphql.type.AdvancedAnalyticsRequest;
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
class AdvancedAnalyticsPanelServiceTest {

    private final String NUMERATOR = "covid19_vaccines";
    private final String DENOMINATOR1 = "area_km2";
    private final String DENOMINATOR2 = "total_building_count";
    private final String NUMERATOR_LABEL = "COVID19 Vaccine Acceptance";
    private final String DENOMINATOR_LABEL1 = "Area";
    private final String DENOMINATOR_LABEL2 = "Area";

    private final String CALCULATION_TYPE1 = "sum";
    private final String CALCULATION_TYPE2 = "mean";

    private final Double VALUE = 0.0;
    private final Double QUALITY = 0.6;

    @Mock
    private InsightsApiGraphqlClient insightsApiGraphqlClient;

    @InjectMocks
    private AdvancedAnalyticsPanelService service;

    @Test
    public void calculateAnalyticsTest() {

        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        var analyticsList = List.of(
                new AdvancedAnalyticalPanelQuery.Analytic("value1", VALUE, CALCULATION_TYPE1, QUALITY),
                new AdvancedAnalyticalPanelQuery.Analytic("value2", VALUE, CALCULATION_TYPE2, QUALITY)
        );

        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> analyticsResults = List.of(
                new AdvancedAnalyticalPanelQuery.AdvancedAnalytic(
                       "analytics1",NUMERATOR, DENOMINATOR1, NUMERATOR_LABEL, DENOMINATOR_LABEL1,
                        analyticsList),
                new AdvancedAnalyticalPanelQuery.AdvancedAnalytic(
                        "analytics1",NUMERATOR, DENOMINATOR2, NUMERATOR_LABEL, DENOMINATOR_LABEL2,
                        analyticsList));

        CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> completableFuture = new CompletableFuture();
        completableFuture.complete(analyticsResults);

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);
        ArgumentCaptor<List<AdvancedAnalyticsRequest>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        when(insightsApiGraphqlClient.advancedAnalyticsPanelQuery(geoJSONArgumentCaptor.capture(), listArgumentCaptor.capture())).thenReturn(completableFuture);

        //when
        AdvancedAnalyticsRequestDto requestDto = new AdvancedAnalyticsRequestDto();
        requestDto.setFeatures(GeoJSONFactory.create(geoJsonString));
        List<AdvancedAnalyticsDto> result = service.calculateAnalytics(requestDto);

        //then
        GeoJSON geoJSONCaptorValue = geoJSONArgumentCaptor.getValue();
        assertEquals(GeoJSONFactory.create(geoJsonString).toString(), geoJSONCaptorValue.toString());

        //check first analytics
        assertEquals(2, result.size());
        AdvancedAnalyticsDto result1 = result.get(0);
        List<AdvancedAnalyticsValuesDto> analyticsValueList1 = result1.getAnalytics();
        assertEquals(2, analyticsValueList1.size());

        assertEquals(NUMERATOR, result1.getNumerator());
        assertEquals(DENOMINATOR1, result1.getDenominator());
        assertEquals(NUMERATOR_LABEL, result1.getNumeratorLabel());
        assertEquals(DENOMINATOR_LABEL1, result1.getDenominatorLabel());

        //check analytics values
        assertEquals(2, result1.getAnalytics().size());
        AdvancedAnalyticsValuesDto advancedAnalyticsValues1 = analyticsValueList1.get(0);
        assertEquals(VALUE, advancedAnalyticsValues1.getValue());
        assertEquals(QUALITY, advancedAnalyticsValues1.getQuality());
        assertEquals(CALCULATION_TYPE1, advancedAnalyticsValues1.getCalculation());

        AdvancedAnalyticsValuesDto advancedAnalyticsValues2 = analyticsValueList1.get(1);
        assertEquals(VALUE, advancedAnalyticsValues2.getValue());
        assertEquals(QUALITY, advancedAnalyticsValues2.getQuality());
        assertEquals(CALCULATION_TYPE2, advancedAnalyticsValues2.getCalculation());

        //check 2nd analytics
        AdvancedAnalyticsDto result2 = result.get(1);
        List<AdvancedAnalyticsValuesDto> analyticsValueList2 = result2.getAnalytics();
        assertEquals(2, analyticsValueList2.size());

        assertEquals(2, result2.getAnalytics().size());
        assertEquals(NUMERATOR, result2.getNumerator());
        assertEquals(DENOMINATOR2, result2.getDenominator());
        assertEquals(NUMERATOR_LABEL, result2.getNumeratorLabel());
        assertEquals(DENOMINATOR_LABEL2, result2.getDenominatorLabel());

        //check 2nd analytic values
        AdvancedAnalyticsValuesDto advancedAnalyticsValues3 = analyticsValueList2.get(0);
        assertEquals(VALUE, advancedAnalyticsValues3.getValue());
        assertEquals(QUALITY, advancedAnalyticsValues3.getQuality());
        assertEquals(CALCULATION_TYPE1, advancedAnalyticsValues3.getCalculation());

        AdvancedAnalyticsValuesDto advancedAnalyticsValues4 = analyticsValueList2.get(1);
        assertEquals(VALUE, advancedAnalyticsValues4.getValue());
        assertEquals(QUALITY, advancedAnalyticsValues4.getQuality());
        assertEquals(CALCULATION_TYPE2, advancedAnalyticsValues4.getCalculation());
    }

    @Test
    public void calculateAnalyticsTestException() throws ExecutionException, InterruptedException {
        var analyticsList = List.of(
                new AdvancedAnalyticalPanelQuery.Analytic("value1", VALUE, CALCULATION_TYPE1, QUALITY),
                new AdvancedAnalyticalPanelQuery.Analytic("value2", VALUE, CALCULATION_TYPE2, QUALITY)
        );
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;

        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> analyticsResults = List.of(
                new AdvancedAnalyticalPanelQuery.AdvancedAnalytic(
                        "analytics1",NUMERATOR, DENOMINATOR1, NUMERATOR_LABEL, DENOMINATOR_LABEL1,
                        analyticsList),
                new AdvancedAnalyticalPanelQuery.AdvancedAnalytic(
                        "analytics1",NUMERATOR, DENOMINATOR2, NUMERATOR_LABEL, DENOMINATOR_LABEL2,
                        analyticsList));

        CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> completableFuture = mock(CompletableFuture.class);

        when(insightsApiGraphqlClient.advancedAnalyticsPanelQuery(any(GeoJSON.class), anyList())).thenReturn(completableFuture);
        when(completableFuture.get()).thenThrow(new InterruptedException());

        //when
        try {
            AdvancedAnalyticsRequestDto requestDto = new AdvancedAnalyticsRequestDto();
            requestDto.setFeatures(GeoJSONFactory.create(geoJsonString));
            service.calculateAnalytics(requestDto);
            throw new RuntimeException("expected exception was not thrown");
        } catch (WebApplicationException e) {
            assertEquals("Exception when getting data from insights-api using apollo client", e.getMessage());
        }
    }
}