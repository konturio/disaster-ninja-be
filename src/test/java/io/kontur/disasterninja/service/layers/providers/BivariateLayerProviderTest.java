package io.kontur.disasterninja.service.layers.providers;

import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import io.kontur.disasterninja.mapper.BivariateStatisticMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.kontur.disasterninja.domain.enums.LayerType.VECTOR;
import static io.kontur.disasterninja.service.layers.providers.BivariateLayerProvider.LAYER_PREFIX;
import static io.kontur.disasterninja.util.TestUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class BivariateLayerProviderTest extends LayerProvidersTest {

    @MockBean
    InsightsApiGraphqlClient insightsApiGraphqlClient;

    BivariateStatisticMapper mapper = Mappers.getMapper(BivariateStatisticMapper.class);

    @BeforeEach
    private void init() {
        BivariateLayerLegendQuery.Overlay overlay = new BivariateLayerLegendQuery
                .Overlay("Overlay", "Kontur OpenStreetMap Quantity",
                "This map shows relative distribution of OpenStreetMap objects and Population. Last updated" +
                        " 2021-11-06T20:59:29Z",
                new BivariateLayerLegendQuery.X("Axis", "OSM objects (n/km²)", steps(0d, 1d, 2d, 1000d,
                        "label1", "label2", "label3", "label4"),
                        List.of(new BivariateLayerLegendQuery.Quotient("Quotient", "count", "OSM Objects", "🗺", 8, "", List.of(), List.of(),
                                        new BivariateLayerLegendQuery.Unit("", "n", "n", "number")),
                                new BivariateLayerLegendQuery.Quotient("Quotient", "area_km2", "Area", "📐", 8, "", List.of(), List.of(),
                                        new BivariateLayerLegendQuery.Unit("", "km2", "km²", "square kilometers"))),
                        List.of("count", "area_km2")),
                new BivariateLayerLegendQuery.Y("Axis", "Population (ppl/km²)", steps1(0d, 10d, 20d, 10000d,
                        "label11", "label12", "label13", "label14"),
                        List.of(new BivariateLayerLegendQuery.Quotient1("Quotient", "population", "Population", "🐱", 8, "", List.of(), List.of(),
                                        new BivariateLayerLegendQuery.Unit1("", "ppl", "ppl", "people")),
                                new BivariateLayerLegendQuery.Quotient1("Quotient", "area_km2", "Area", "📐", 8, "", List.of(), List.of(),
                                        new BivariateLayerLegendQuery.Unit1("", "km2", "km²", "square kilometers"))),
                        List.of("population", "area_km2")),
                List.of(new BivariateLayerLegendQuery.Color("OverlayColor", "A1", "rgb(111,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "A2", "rgb(222,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "A3", "rgb(333,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B1", "rgb(232,111,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B2", "rgb(232,222,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B3", "rgb(232,333,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C1", "rgb(232,232,111)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C2", "rgb(232,232,222)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C3", "rgb(232,232,333)")),
                1
        );
        List<BivariateLayerLegendQuery.Indicator> indicators = List.of(
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "area_km2", "Area", null, List.of(List.of("neutral"), List.of("neutral")),
                        List.of("copyrights https://kontur.io/"),
                        new BivariateLayerLegendQuery.Unit2("", "km2", "km²", "square kilometers")),
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "count", "OSM Objects", null, List.of(List.of("bad"), List.of("good")), List.of("copyrights1"),
                        new BivariateLayerLegendQuery.Unit2("", "n", "n", "number")),
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "population", "Population", null, List.of(List.of("unimportant"), List.of("important")),
                        List.of("copyrights1", "copyrights2"),
                        new BivariateLayerLegendQuery.Unit2("", "ppl", "ppl", "people")));

        BivariateStatisticDto dto = BivariateStatisticDto.builder()
                .overlays(mapper.bivariateLayerLegendQueryOverlayListToOverlayDtoList(List.of(overlay)))
                .indicators(mapper.bivariateLayerLegendQueryIndicatorListToIndicatorDtoList(indicators))
                .build();

        Mockito.when(insightsApiGraphqlClient.getBivariateStatistic()).thenReturn(CompletableFuture
                .completedFuture(dto));
        bivariateLayerProvider = new BivariateLayerProvider(insightsApiGraphqlClient);
    }

    @Test
    public void bivariateListExceptionTest() throws ExecutionException, InterruptedException {
        when(insightsApiGraphqlClient.getBivariateStatistic()).thenThrow(new ApolloException("hello world"));

        assertTrue(bivariateLayerProvider.obtainGlobalLayers(emptyParams()).get().isEmpty());
    }

    @Test
    public void bivariateLayerExceptionTest() {
        when(insightsApiGraphqlClient.getBivariateStatistic()).thenThrow(new ApolloException("hello world"));

        assertNull(bivariateLayerProvider.obtainLayer("some layer", emptyParams()));
    }

    @Test
    public void obtainGlobalLayersTest() throws ExecutionException, InterruptedException {
        List<Layer> layers = bivariateLayerProvider.obtainGlobalLayers(emptyParams()).get();
        assertEquals(1, layers.size());
    }

    @Test
    public void obtainUserLayersTest() throws ExecutionException, InterruptedException {
        List<Layer> layers = bivariateLayerProvider.obtainUserLayers(paramsWithSomeAppId()).get();
        assertTrue(layers.isEmpty());
    }

    @Test
    public void obtainSelectedAreaLayersTest() throws ExecutionException, InterruptedException {
        List<Layer> layers = bivariateLayerProvider.obtainSelectedAreaLayers(paramsWithSomeBoundary()).get();
        assertTrue(layers.isEmpty());
    }

    @Test
    public void getTest() {
        Layer biv = bivariateLayerProvider.obtainLayer(LAYER_PREFIX + "Kontur OpenStreetMap Quantity", emptyParams());
        //layer
        assertEquals(LAYER_PREFIX + "Kontur OpenStreetMap Quantity", biv.getId());
        assertEquals("Kontur OpenStreetMap Quantity", biv.getName());
        assertEquals("This map shows relative distribution of OpenStreetMap objects and Population." +
                " Last updated 2021-11-06T20:59:29Z", biv.getDescription());

        //source
        assertEquals(VECTOR, biv.getSource().getType());

        //legend
        assertNotNull(biv.getLegend());
        assertNotNull(biv.getLegend().getAxes());

        //axisX
        BivariateLegendAxisDescription x = biv.getLegend().getAxes().getX();
        assertEquals("OSM objects (n/km²)", x.getLabel());
        assertEquals(2, x.getQuotients().size());
        assertEquals(2, x.getQuotients().stream().filter(q -> "count".equals(q.getName()) || "area_km2".equals(q.getName())).count());
        assertEquals(2, x.getQuotient().size());
        assertEquals(2, x.getQuotient().stream().filter(q -> "count".equals(q) || "area_km2".equals(q)).count());
        assertEquals(4, x.getSteps().stream().filter(q -> (q.getValue().equals(0d) && (q.getLabel().equals("label1")))
                || (q.getValue().equals(1d) && q.getLabel().equals("label2")) || (q.getValue()
                .equals(2d) && q.getLabel().equals("label3"))
                || (q.getValue().equals(1000d) && q.getLabel().equals("label4"))).count());


        //axisY
        BivariateLegendAxisDescription y = biv.getLegend().getAxes().getY();
        assertEquals("Population (ppl/km²)", y.getLabel());
        assertEquals(2, y.getQuotients().size());
        assertEquals(2, y.getQuotients().stream().filter(q -> "population".equals(q.getName()) || "area_km2".equals(q.getName())).count());
        assertEquals(2, y.getQuotient().size());
        assertEquals(2, y.getQuotient().stream().filter(q -> "population".equals(q) || "area_km2".equals(q)).count());
        assertEquals(4, y.getSteps().stream().filter(q -> (q.getValue().equals(0d) && q.getLabel().equals("label11"))
                || (q.getValue().equals(10d) && q.getLabel().equals("label12")) || (q.getValue()
                .equals(20d) && q.getLabel().equals("label13"))
                || (q.getValue().equals(10000d) && q.getLabel().equals("label14"))).count());

        //skipping other params

        //colors
        assertNotNull(biv.getLegend().getColors());
        assertEquals(9, biv.getLegend().getColors().size());

        assertEquals("A1", biv.getLegend().getColors().get(0).getId());
        assertEquals("rgb(111,232,157)", biv.getLegend().getColors().get(0).getColor());

        //copyrights
        assertEquals(3, biv.getCopyrights().size());

    }

    @Test
    public void copyrightLinkMarkdown_8657() throws ExecutionException, InterruptedException {
        List<Layer> layers = bivariateLayerProvider.obtainGlobalLayers(emptyParams()).get();
        assertThat(layers.get(0).getCopyrights(),
                hasItems("copyrights [https://kontur.io/](https://kontur.io/)", "copyrights1", "copyrights2"));
    }

    private List<BivariateLayerLegendQuery.Step> steps(double d1, double d2, double d3, double d4,
                                                       String str1, String str2, String str3, String str4) {
        BivariateLayerLegendQuery.Step s1 = new BivariateLayerLegendQuery.Step("Step", str1, d1);
        BivariateLayerLegendQuery.Step s2 = new BivariateLayerLegendQuery.Step("Step", str2, d2);
        BivariateLayerLegendQuery.Step s3 = new BivariateLayerLegendQuery.Step("Step", str3, d3);
        BivariateLayerLegendQuery.Step s4 = new BivariateLayerLegendQuery.Step("Step", str4, d4);
        return List.of(s1, s2, s3, s4);
    }

    private List<BivariateLayerLegendQuery.Step1> steps1(double d1, double d2, double d3, double d4,
                                                         String str1, String str2, String str3, String str4) {
        BivariateLayerLegendQuery.Step1 s1 = new BivariateLayerLegendQuery.Step1("Step", str1, d1);
        BivariateLayerLegendQuery.Step1 s2 = new BivariateLayerLegendQuery.Step1("Step", str2, d2);
        BivariateLayerLegendQuery.Step1 s3 = new BivariateLayerLegendQuery.Step1("Step", str3, d3);
        BivariateLayerLegendQuery.Step1 s4 = new BivariateLayerLegendQuery.Step1("Step", str4, d4);
        return List.of(s1, s2, s3, s4);
    }
}
