package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.Point;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class BivariateLayerProviderTest extends LayerProvidersTest {

    @MockBean
    InsightsApiGraphqlClient insightsApiGraphqlClient;

    @BeforeEach
    private void init() {
        BivariateLayerLegendQuery.Overlay overlay = new BivariateLayerLegendQuery
            .Overlay("Overlay", "Kontur OpenStreetMap Quantity",
            "This map shows relative distribution of OpenStreetMap objects and Population. Last updated" +
                " 2021-11-06T20:59:29Z",
            new BivariateLayerLegendQuery.X("Axis", "OSM objects (n/km²)", steps(0d, 1d, 2d, 1000d),
                List.of("count", "area_km2")),
            new BivariateLayerLegendQuery.Y("Axis", "Population (ppl/km²)", steps1(0d, 10d, 20d, 10000d),
                List.of("count", "area_km2")),
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

        Mockito.when(insightsApiGraphqlClient.getBivariateOverlays()).thenReturn(CompletableFuture
            .completedFuture(List.of(overlay)));
    }

    @Test
    public void list() {
        List<Layer> layers = bivariateLayerProvider.obtainLayers(null, null);
        assertEquals(1, layers.size());
    }

    @Test
    public void get() {
        Layer biv = bivariateLayerProvider.obtainLayer(new Point(new double[]{0d, 0d}),
            "Kontur OpenStreetMap Quantity", null);
        //layer
        assertEquals("Kontur OpenStreetMap Quantity", biv.getId());
        assertEquals("This map shows relative distribution of OpenStreetMap objects and Population." +
            " Last updated 2021-11-06T20:59:29Z", biv.getDescription());

        //source
        assertEquals(VECTOR, biv.getSource().getType());

        //legend
        assertNotNull(biv.getLegend());
        assertEquals(8, biv.getLegend().getSteps().size());
        //step1
        LegendStep xStep1 = biv.getLegend().getSteps().get(0);
        assertEquals(0, xStep1.getOrder());
        assertNull(xStep1.getParamName()); //axis is used instead
        assertNull(xStep1.getParamValue()); //axisValue is used instead
        assertEquals("X", xStep1.getAxis());
        assertEquals(0.0d, xStep1.getAxisValue());
        assertNull(xStep1.getStepName());
        assertNull(xStep1.getStepShape());
        assertNull(xStep1.getStyle());
        //last step for Y axis
        LegendStep yStep = biv.getLegend().getSteps().get(7);
        assertEquals("Y", yStep.getAxis());
        assertEquals(10000d, yStep.getAxisValue());
        //skipping other params

        //colors
        assertNotNull(biv.getLegend().getBivariateColors());
        assertEquals(9, biv.getLegend().getBivariateColors().entrySet().size());

        String a1 = biv.getLegend().getBivariateColors().get("A1");
        assertEquals("rgb(111,232,157)", a1);
        String c3 = biv.getLegend().getBivariateColors().get("C3");
        assertEquals("rgb(232,232,333)", c3);
    }

    private List<BivariateLayerLegendQuery.Step> steps(double d1, double d2, double d3, double d4) {
        BivariateLayerLegendQuery.Step s1 = new BivariateLayerLegendQuery.Step("Step", null, d1);
        BivariateLayerLegendQuery.Step s2 = new BivariateLayerLegendQuery.Step("Step", null, d2);
        BivariateLayerLegendQuery.Step s3 = new BivariateLayerLegendQuery.Step("Step", null, d3);
        BivariateLayerLegendQuery.Step s4 = new BivariateLayerLegendQuery.Step("Step", null, d4);
        return List.of(s1, s2, s3, s4);
    }

    private List<BivariateLayerLegendQuery.Step1> steps1(double d1, double d2, double d3, double d4) {
        BivariateLayerLegendQuery.Step1 s1 = new BivariateLayerLegendQuery.Step1("Step", null, d1);
        BivariateLayerLegendQuery.Step1 s2 = new BivariateLayerLegendQuery.Step1("Step", null, d2);
        BivariateLayerLegendQuery.Step1 s3 = new BivariateLayerLegendQuery.Step1("Step", null, d3);
        BivariateLayerLegendQuery.Step1 s4 = new BivariateLayerLegendQuery.Step1("Step", null, d4);
        return List.of(s1, s2, s3, s4);
    }
}
