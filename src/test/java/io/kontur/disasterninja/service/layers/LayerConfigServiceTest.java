package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Point;

import java.util.HashMap;
import java.util.Map;

import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.domain.enums.LayerStepShape.CIRCLE;
import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;
import static io.kontur.disasterninja.service.layers.providers.HotLayerProvider.HOT_ID;

@SpringBootTest
public class LayerConfigServiceTest {
    @Autowired
    LocalLayerConfigService service;

    private static Feature feature(String paramName, Object paramValue) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(paramName, paramValue);
        return new Feature(new Point(new double[]{1, 2}), properties);
    }

    @Test
    public void hotWithFeaturesForAllStepsTest() {
        Layer hot = Layer.builder()
            .id(HOT_ID)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(
                    new Feature[]{
                        feature("status", "Active"),
                        feature("status", "Archived"),
                        feature("status", "Published")}
                )).build()).build();
        service.applyConfig(hot);

        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        // all 3 steps are present since there is at least one feature for each step
        Assertions.assertEquals(3, hot.getLegend().getSteps().size());

        //steps
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("Published", hot.getLegend().getSteps().get(2).getStepName());
        //step 1
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getParamValue());
        //step 2
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(1).getParamName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getParamValue());
        //step 3
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(2).getParamName());
        Assertions.assertEquals("Published", hot.getLegend().getSteps().get(2).getParamValue());

        Assertions.assertEquals("link_to_icon", hot.getLegend().getSteps().get(0).getStyle().get("icon"));
        Assertions.assertEquals("link_to_icon_2", hot.getLegend().getSteps().get(1).getStyle().get("icon"));
        Assertions.assertEquals("link_to_icon", hot.getLegend().getSteps().get(2).getStyle().get("icon"));
    }

    @Test
    public void hotWithFeaturesForSomeStepsTest() {
        Layer hot = Layer.builder()
            .id(HOT_ID)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(
                    new Feature[]{
                        feature("status", "Active"),
                        feature("status", "Archived")}
                )).build()).build();
        //new Layer(HOT_ID, true);
        service.applyConfig(hot);

        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        // just two steps are present since there are no features for step 3 (Published)
        Assertions.assertEquals(2, hot.getLegend().getSteps().size());

        //steps
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getStepName());
        //step 1
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("Active", hot.getLegend().getSteps().get(0).getParamValue());
        //step 2
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(1).getParamName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getParamValue());

        Assertions.assertEquals("link_to_icon", hot.getLegend().getSteps().get(0).getStyle().get("icon"));
        Assertions.assertEquals("link_to_icon_2", hot.getLegend().getSteps().get(1).getStyle().get("icon"));
    }

    @Test
    public void urbanTest() {
        Layer urban = Layer.builder()
            .id("kontur_settled_periphery")
            .build();
        service.applyConfig(urban);

        Assertions.assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows a " +
            "spread-out part of the population in the region. For this event it adds {{population}} people on" +
            " {{areaKm2}}km² on top of Kontur Urban Core.", urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
    }

    @Test
    public void eventShapeTest() {
        Layer eventShape = Layer.builder()
            .id("eventShape")
            .build();
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
    public void activeContributorsWithoutFeaturesTest() {
        Layer activeContributors = Layer.builder()
            .id("activeContributors")
            .source(LayerSource.builder().data(new FeatureCollection(null)).build())
            .build();
        service.applyConfig(activeContributors);
        //layer
        Assertions.assertEquals("Active contributors", activeContributors.getName());
        Assertions.assertEquals("Kontur", activeContributors.getGroup());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        //legend
        Assertions.assertNotNull(activeContributors.getLegend());
        Assertions.assertEquals(SIMPLE, activeContributors.getLegend().getType());
        //steps
        //empty Legend since no features exist for defined steps
        Assertions.assertEquals(0, activeContributors.getLegend().getSteps().size());
        //skipping other fields
    }

    @Test
    public void konturAnalyticsTest() {
        Layer activeContributors = Layer.builder()
            .id("osmObjectQuantity")
            .build();
        service.applyConfig(activeContributors);
        //layer
        Assertions.assertEquals("OSM Object Quantity", activeContributors.getName());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        Assertions.assertEquals("Kontur Analytical Layers", activeContributors.getGroup());
    }
}
