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
import java.util.List;
import java.util.Map;

import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;
import static io.kontur.disasterninja.dto.EventType.CYCLONE;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.EVENT_SHAPE_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.HOT_LAYER_ID;

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
    public void globalOverlaysTest() {
        Assertions.assertFalse(service.getGlobalOverlays().isEmpty());
    }

    @Test
    public void hotWithFeaturesForAllStepsTest() {
        Layer hot = Layer.builder()
            .id(HOT_LAYER_ID)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(
                    new Feature[]{
                        feature("status", "Active"),
                        feature("status", "Archived"),
                        feature("status", "Published")}
                )).build()).build();
        service.applyConfig(hot);

        Assertions.assertFalse(hot.isGlobalOverlay());
        Assertions.assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        //colors are only used for bivariate legends
        Assertions.assertNull(hot.getLegend().getBivariateColors());
        Assertions.assertEquals("projectLink", hot.getLegend().getLinkProperty());
        // all 3 steps are present since there is at least one feature for each step
        Assertions.assertEquals(2, hot.getLegend().getSteps().size());

        //steps
        Assertions.assertEquals("Published", hot.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(1).getStepName());
        //step 1
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("PUBLISHED", hot.getLegend().getSteps().get(0).getParamValue());
        //step 2
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(1).getParamName());
        Assertions.assertEquals("ARCHIVED", hot.getLegend().getSteps().get(1).getParamValue());

        Assertions.assertEquals("hot-red", hot.getLegend().getSteps().get(0).getStyle().get("icon"));
        Assertions.assertEquals("hot-gray", hot.getLegend().getSteps().get(1).getStyle().get("icon"));
    }

    @Test
    public void hotWithFeaturesForSomeStepsTest() {
        Layer hot = Layer.builder()
            .id(HOT_LAYER_ID)
            .source(LayerSource.builder()
                .type(GEOJSON)
                .data(new FeatureCollection(
                    new Feature[]{
                        feature("status", "Archived"),
                        feature("status", "Archived")}
                )).build()).build();
        service.applyConfig(hot);

        Assertions.assertFalse(hot.isGlobalOverlay());
        Assertions.assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertEquals("projectLink", hot.getLegend().getLinkProperty());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        //colors are only used for bivariate legends
        Assertions.assertNull(hot.getLegend().getBivariateColors());
        // just one step is present since there are no features for step 2 (Published)
        Assertions.assertEquals(1, hot.getLegend().getSteps().size());

        //steps
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(0).getStepName());
        //step 1
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("ARCHIVED", hot.getLegend().getSteps().get(0).getParamValue());

        Assertions.assertEquals("hot-gray", hot.getLegend().getSteps().get(0).getStyle().get("icon"));
    }

    @Test
    public void hotWithoutFeaturesTest() {
        Layer hot = Layer.builder()
            .id(HOT_LAYER_ID)
            .build();
        service.applyConfig(hot);

        Assertions.assertFalse(hot.isGlobalOverlay());
        Assertions.assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Hot Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertEquals(OVERLAY, hot.getCategory());
        Assertions.assertEquals("Kontur", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertEquals("projectLink", hot.getLegend().getLinkProperty());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        //empty Legend since no features exist for defined steps
        Assertions.assertEquals(0, hot.getLegend().getSteps().size());
    }

    @Test
    public void urbanTest() {
        Layer urban = Layer.builder()
            .id("kontur_urban_core")
            .build();
        service.applyConfig(urban);

        Assertions.assertFalse(urban.isGlobalOverlay());
        Assertions.assertTrue(urban.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertTrue(urban.isBoundaryRequiredForRetrieval());
        //description is not populated for this layer (it's populated by LayerProvider and not changed by configs)
        Assertions.assertNull(urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
    }

    @Test
    public void settledPeripheryTest() {
        Layer urban = Layer.builder()
            .id("kontur_settled_periphery")
            .build();
        service.applyConfig(urban);

        Assertions.assertFalse(urban.isGlobalOverlay());
        Assertions.assertTrue(urban.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertTrue(urban.isBoundaryRequiredForRetrieval());
        //description is not populated for this layer (it's populated by LayerProvider and not changed by configs)
        Assertions.assertNull(urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
    }

    @Test
    public void activeContributorsWithoutFeaturesTest() {
        Layer activeContributors = Layer.builder()
            .id("activeContributors")
            .source(LayerSource.builder().data(new FeatureCollection(null)).build())
            .build();
        service.applyConfig(activeContributors);
        //layer
        Assertions.assertTrue(activeContributors.isGlobalOverlay());
        Assertions.assertTrue(activeContributors.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertFalse(activeContributors.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Active contributors", activeContributors.getName());
        Assertions.assertEquals("Kontur", activeContributors.getGroup());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        //legend
        Assertions.assertNotNull(activeContributors.getLegend());
        Assertions.assertEquals(SIMPLE, activeContributors.getLegend().getType());
        Assertions.assertNull(activeContributors.getLegend().getLinkProperty()); //not used in this layer
        //steps
        //Steps are always shown since displayLegendIfNoFeaturesExist is true
        Assertions.assertEquals(3, activeContributors.getLegend().getSteps().size());
        Assertions.assertEquals("users", activeContributors.getLegend().getSteps().get(0).getSourceLayer());
        //skipping other fields
    }

    @Test
    public void bingUrlsTest() {
        Layer bing = Layer.builder().id("Bing").build();
        service.applyConfig(bing);
        Assertions.assertEquals(8, bing.getSource().getUrls().size());
    }

    @Test
    public void konturAnalyticsTest() {
        Layer analytics = Layer.builder()
            .id("Kontur OpenStreetMap Quantity")
            .build();
        service.applyConfig(analytics);
        //layer
        Assertions.assertEquals(List.of("https://test-apps02.konturlabs.com/tiles/stats/{x}/{y}/{z}.mvt"),
            analytics.getSource().getUrls());
        Assertions.assertTrue(analytics.isGlobalOverlay());
        Assertions.assertFalse(analytics.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertFalse(analytics.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("OSM Object Quantity", analytics.getName());
        Assertions.assertEquals(OVERLAY, analytics.getCategory());
        Assertions.assertEquals("Kontur Analytical Layers", analytics.getGroup());
        //copyrights
        Assertions.assertEquals(4, analytics.getCopyrights().size());
        Assertions.assertEquals("© OpenStreetMap contributors https://www.openstreetmap.org/copyright",
            analytics.getCopyrights().get(0));
        //skipping the rest
    }

    //even shape tests

    @Test
    public void eventShapeTest() {
        //base config params
        Layer eventShape = Layer.builder()
            .id(EVENT_SHAPE_LAYER_ID)
            .build();
        service.applyConfig(eventShape);
        //layer
        Assertions.assertFalse(eventShape.isGlobalOverlay());
        Assertions.assertTrue(eventShape.isDisplayLegendIfNoFeaturesExist());
        Assertions.assertFalse(eventShape.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Event shape", eventShape.getName());
        Assertions.assertEquals("Layers in selected area", eventShape.getGroup());
        //legend
        Assertions.assertNotNull(eventShape.getLegend());
        Assertions.assertEquals(SIMPLE, eventShape.getLegend().getType());
        //steps
        Assertions.assertEquals(1, eventShape.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", eventShape.getLegend().getSteps().get(0).getStepName());
        //skipping other fields
    }

    @Test
    public void eventShapeDefaultTest() {
        //eventShape without 'Class' property in features - uses basic config
        Layer layer = Layer.builder()
            .id(EVENT_SHAPE_LAYER_ID)
            .source(LayerSource.builder()
                .data(new FeatureCollection(new Feature[]{feature("some", "value")}))
                .build())
            .build();

        service.applyConfig(layer);

        Assertions.assertNotNull(layer.getLegend());
        Assertions.assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertFalse(layer.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", layer.getLegend().getSteps().get(0).getStepName());
        //skipping other fields
    }

    @Test
    public void eventShapeCycloneAllStepsTest() {
        //event type = CYCLONE
        //features with different Class value exist - each should be added to legend
        Layer layer = Layer.builder()
            .id(EVENT_SHAPE_LAYER_ID + "." + CYCLONE)
            .source(LayerSource.builder()
                .data(new FeatureCollection(new Feature[]{
                    //random order, some duplicates
                    feature("Class", "Point_Centroid"),
                    feature("Class", "Poly_Red"),
                    feature("Class", "Poly_Green"),
                    feature("Class", "Poly_Orange"),
                    feature("Class", "Poly_Orange"),
                    feature("Class", "Poly_Green"),
                    feature("Class", "Point_Polygon_Point_234"),
                    feature("Class", "Poly_Green"),
                    feature("Class", "Line_Line_2"),
                    feature("Class", "Poly_Cones")}
                ))
                .build())
            .build();

        service.applyConfig(layer);
        Assertions.assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        Assertions.assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(7, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Centroid", layer.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Exposure Area 60 km/h", layer.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("Exposure Area 90 km/h", layer.getLegend().getSteps().get(2).getStepName());
        Assertions.assertEquals("Exposure Area 120 km/h", layer.getLegend().getSteps().get(3).getStepName());
        Assertions.assertEquals("Line Track", layer.getLegend().getSteps().get(4).getStepName());
        Assertions.assertEquals("Point Track", layer.getLegend().getSteps().get(5).getStepName());
        Assertions.assertEquals("Uncertainty Cones", layer.getLegend().getSteps().get(6).getStepName());
        //skipping other fields
    }

    @Test
    public void eventShapeCycloneSomeStepsTest() {
        //event type = CYCLONE
        //features with different Class value exist - each should be added to legend
        Layer layer = Layer.builder()
            .id(EVENT_SHAPE_LAYER_ID + "." + CYCLONE)
            .source(LayerSource.builder()
                .data(new FeatureCollection(new Feature[]{
                    //random order, some duplicates
                    feature("Class", "Point_Centroid"),
                    feature("Class", "Point_Polygon_Point_777"),
                    feature("Class", "Poly_Cones")}
                ))
                .build())
            .build();

        service.applyConfig(layer);
        Assertions.assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        Assertions.assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(3, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Centroid", layer.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Point Track", layer.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("Uncertainty Cones", layer.getLegend().getSteps().get(2).getStepName());
        //skipping other fields
    }


}
