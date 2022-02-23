package io.kontur.disasterninja.service.layers.providers;

import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.Point;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class BivariateLayerProviderTest extends LayerProvidersTest {

    @MockBean
    InsightsApiGraphqlClient insightsApiGraphqlClient;

    @BeforeEach
    private void init() {
        BivariateLayerLegendQuery.Overlay overlay = new BivariateLayerLegendQuery
                .Overlay("Overlay", "Kontur OpenStreetMap Quantity",
                "This map shows relative distribution of OpenStreetMap objects and Population. Last updated" +
                        " 2021-11-06T20:59:29Z",
                new BivariateLayerLegendQuery.X("Axis", "OSM objects (n/km²)", steps(0d, 1d, 2d, 1000d,
                        "label1", "label2", "label3", "label4"),
                        List.of("count", "area_km2")),
                new BivariateLayerLegendQuery.Y("Axis", "Population (ppl/km²)", steps1(0d, 10d, 20d, 10000d,
                        "label11", "label12", "label13", "label14"),
                        List.of("population", "area_km2")),
                List.of(new BivariateLayerLegendQuery.Color("OverlayColor", "A1", "rgb(111,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "A2", "rgb(222,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "A3", "rgb(333,232,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B1", "rgb(232,111,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B2", "rgb(232,222,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "B3", "rgb(232,333,157)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C1", "rgb(232,232,111)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C2", "rgb(232,232,222)"),
                        new BivariateLayerLegendQuery.Color("OverlayColor", "C3", "rgb(232,232,333)"))
        );
        List<BivariateLayerLegendQuery.Indicator> indicators = List.of(
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "area_km2", "Area", List.of(List.of("neutral"), List.of("neutral")), List.of("copyrights https://kontur.io/")),
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "count", "OSM Objects", List.of(List.of("bad"), List.of("good")), List.of("copyrights1")),
                new BivariateLayerLegendQuery.Indicator("Indicator",
                        "population", "Population", List.of(List.of("unimportant"), List.of("important")), List.of("copyrights1", "copyrights2")));

        BivariateStatisticDto dto = BivariateStatisticDto.builder()
                .overlays(List.of(overlay))
                .indicators(indicators)
                .build();

        Mockito.when(insightsApiGraphqlClient.getBivariateStatistic()).thenReturn(CompletableFuture
                .completedFuture(dto));
    }

    @Test
    public void bivariateListExceptionTest() {
        when(insightsApiGraphqlClient.getBivariateStatistic()).thenThrow(new ApolloException("hello world"));

        try {
            bivariateLayerProvider.obtainLayers(null, null);
            throw new RuntimeException("expected exception was not thrown");
        } catch (WebApplicationException e) {
            assertEquals("Can't load bivariate layers", e.getMessage());
        }
    }

    @Test
    public void bivariateLayerExceptionTest() {
        when(insightsApiGraphqlClient.getBivariateStatistic()).thenThrow(new ApolloException("hello world"));

        try {
            bivariateLayerProvider.obtainLayer(null, "Kontur Nighttime Heatwave Risk", null);
            throw new RuntimeException("expected exception was not thrown");
        } catch (WebApplicationException e) {
            assertEquals("Can't load bivariate layer", e.getMessage());
        }
    }

    @Test
    public void listTest() {
        List<Layer> layers = bivariateLayerProvider.obtainLayers(null, null);
        assertEquals(1, layers.size());
    }

    @Test
    public void getTest() {
        Layer biv = bivariateLayerProvider.obtainLayer(new Point(new double[]{0d, 0d}),
                "Kontur OpenStreetMap Quantity", null);
        //layer
        assertEquals("Kontur OpenStreetMap Quantity", biv.getId());
        assertEquals("Kontur OpenStreetMap Quantity", biv.getName());
        assertNull(biv.getDescription());
        assertNull(biv.getCopyrights());

        //source
        assertEquals(VECTOR, biv.getSource().getType());

        //legend
        assertNotNull(biv.getLegend());
        assertNotNull(biv.getLegend().getBivariateAxes());
        assertEquals("Kontur OpenStreetMap Quantity", biv.getLegend().getName());
        assertEquals("This map shows relative distribution of OpenStreetMap objects and Population." +
                " Last updated 2021-11-06T20:59:29Z", biv.getLegend().getDescription());

        //axisX
        BivariateLegendAxisDescription x = biv.getLegend().getBivariateAxes().getX();
        assertEquals("OSM objects (n/km²)", x.getLabel());
        assertEquals(2, x.getQuotient().size());
        assertEquals(2, x.getQuotient().stream().filter(q -> q.equals("count") || q.equals("area_km2")).count());
        assertEquals(4, x.getSteps().stream().filter(q -> (q.getValue().equals(0d) && (q.getLabel().equals("label1")))
                || (q.getValue().equals(1d) && q.getLabel().equals("label2")) || (q.getValue().equals(2d) && q.getLabel().equals("label3"))
                || (q.getValue().equals(1000d) && q.getLabel().equals("label4"))).count());


        //axisY
        BivariateLegendAxisDescription y = biv.getLegend().getBivariateAxes().getY();
        assertEquals("Population (ppl/km²)", y.getLabel());
        assertEquals(2, y.getQuotient().size());
        assertEquals(2, y.getQuotient().stream().filter(q -> q.equals("population") || q.equals("area_km2")).count());
        assertEquals(4, y.getSteps().stream().filter(q -> (q.getValue().equals(0d) && q.getLabel().equals("label11"))
                || (q.getValue().equals(10d) && q.getLabel().equals("label12")) || (q.getValue().equals(20d) && q.getLabel().equals("label13"))
                || (q.getValue().equals(10000d) && q.getLabel().equals("label14"))).count());

        //skipping other params

        //colors
        assertNotNull(biv.getLegend().getBivariateColors());
        assertEquals(9, biv.getLegend().getBivariateColors().entrySet().size());

        String a1 = biv.getLegend().getBivariateColors().get("A1");
        assertEquals("rgb(111,232,157)", a1);
        String c3 = biv.getLegend().getBivariateColors().get("C3");
        assertEquals("rgb(232,232,333)", c3);

        //copyrights
        assertEquals(3, biv.getLegend().getCopyrights().size());

    }

    @Test
    public void copyrightLinkMarkdown_8657() {
        List<Layer> layers = bivariateLayerProvider.obtainLayers(null, null);
        assertThat(layers.get(0).getLegend().getCopyrights(), hasItems("copyrights [https://kontur.io/](https://kontur.io/)", "copyrights1", "copyrights2"));
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
