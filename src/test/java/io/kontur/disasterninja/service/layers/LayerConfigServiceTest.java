package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerStepShape.CIRCLE;
import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;
import static io.kontur.disasterninja.service.layers.providers.HotLayerProvider.HOT_ID;

@SpringBootTest
public class LayerConfigServiceTest {
    @Autowired
    LocalLayerConfigService service;

    @Test
    public void hotTest() {
        Layer hot = new Layer(HOT_ID, true);
        service.applyConfig(hot);

        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getStepName());

        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getParamValue());

        Assertions.assertEquals("status", hot.getLegend().getSteps().get(1).getParamName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getParamValue());

        Assertions.assertEquals("link_to_icon", hot.getLegend().getSteps().get(0).getStyle().get("icon"));
        Assertions.assertEquals("link_to_icon_2", hot.getLegend().getSteps().get(1).getStyle().get("icon"));
    }

    @Test
    public void urbanTest() {
        Layer urban = new Layer("kontur_settled_periphery", true);
        service.applyConfig(urban);

        Assertions.assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows a " +
            "spread-out part of the population in the region. For this event it adds {{population}} people on" +
            " {{areaKm2}}km² on top of Kontur Urban Core.", urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
    }

    @Test
    public void eventShapeTest() {
        Layer eventShape = new Layer("eventShape", true);
        service.applyConfig(eventShape);
        //layer
        Assertions.assertEquals("Event shape", eventShape.getName());
        Assertions.assertEquals("Layers in selected area", eventShape.getGroup());
        //legend
        Assertions.assertNotNull(eventShape.getLegend());
        Assertions.assertEquals(SIMPLE, eventShape.getLegend().getType());
        //steps
        Assertions.assertEquals(3, eventShape.getLegend().getSteps().size());
        Assertions.assertEquals("Moderate", eventShape.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals(CIRCLE, eventShape.getLegend().getSteps().get(0).getStepShape());
        //skipping other fields
    }

    @Test
    public void activeContributorsTest() {
        Layer activeContributors = new Layer("activeContributors", true);
        service.applyConfig(activeContributors);
        //layer
        Assertions.assertEquals("Active contributors", activeContributors.getName());
        Assertions.assertEquals("Kontur", activeContributors.getGroup());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        //legend
        Assertions.assertNotNull(activeContributors.getLegend());
        Assertions.assertEquals(SIMPLE, activeContributors.getLegend().getType());
        //steps
        Assertions.assertEquals(2, activeContributors.getLegend().getSteps().size());
        Assertions.assertEquals("Active mappers", activeContributors.getLegend().getSteps().get(1)
            .getStepName());
        Assertions.assertEquals(CIRCLE, activeContributors.getLegend().getSteps().get(1).getStepShape());
        //skipping other fields
    }


    @Test
    public void konturAnalyticsTest() {
        Layer activeContributors = new Layer("osmAnalytics1", true);
        service.applyConfig(activeContributors);
        //layer
        Assertions.assertEquals("OSM Object Quantity", activeContributors.getName());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        Assertions.assertEquals("Kontur Analytical Layers", activeContributors.getGroup());
    }
}
