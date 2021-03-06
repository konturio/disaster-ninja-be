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

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;
import static io.kontur.disasterninja.dto.EventType.*;
import static io.kontur.disasterninja.service.layers.providers.EventShapeLayerProvider.EVENT_SHAPE_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.HotLayerProvider.HOT_LAYER_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(service.getGlobalOverlays().isEmpty());
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

        assertFalse(hot.isGlobalOverlay());
        assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("HOT Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertNull(hot.getCategory());
        Assertions.assertEquals("layersInSelectedArea", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertEquals("HOT Projects", hot.getLegend().getName());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        assertFalse(hot.isEventIdRequiredForRetrieval());
        //colors are only used for bivariate legends
        Assertions.assertNull(hot.getLegend().getColors());
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

        Assertions.assertEquals("hot-red", hot.getLegend().getSteps().get(0).getStyle().get("icon-image"));
        Assertions.assertEquals("hot-gray", hot.getLegend().getSteps().get(1).getStyle().get("icon-image"));

        //#8748 text-offset is a List of Numbers, not Map or String
        Assertions.assertTrue(hot.getLegend().getSteps().get(0).getStyle().get("text-offset") instanceof List);
        Assertions.assertEquals(0, ((List<?>) hot.getLegend().getSteps().get(0).getStyle().get("text-offset")).get(0));
        Assertions.assertEquals(0.6,
                ((List<?>) hot.getLegend().getSteps().get(0).getStyle().get("text-offset")).get(1));
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

        assertFalse(hot.isGlobalOverlay());
        assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("HOT Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertNull(hot.getCategory());
        Assertions.assertEquals("layersInSelectedArea", hot.getGroup());
        Assertions.assertNotNull(hot.getLegend());
        Assertions.assertEquals("projectLink", hot.getLegend().getLinkProperty());
        Assertions.assertNotNull(hot.getLegend().getSteps());
        //colors are only used for bivariate legends
        Assertions.assertNull(hot.getLegend().getColors());
        // just one step is present since there are no features for step 2 (Published)
        Assertions.assertEquals(1, hot.getLegend().getSteps().size());

        //steps
        Assertions.assertEquals("Archived", hot.getLegend().getSteps().get(0).getStepName());
        //step 1
        Assertions.assertEquals("status", hot.getLegend().getSteps().get(0).getParamName());
        Assertions.assertEquals("ARCHIVED", hot.getLegend().getSteps().get(0).getParamValue());

        Assertions.assertEquals("hot-gray", hot.getLegend().getSteps().get(0).getStyle().get("icon-image"));
    }

    @Test
    public void hotWithoutFeaturesTest() {
        Layer hot = Layer.builder()
                .id(HOT_LAYER_ID)
                .build();
        service.applyConfig(hot);

        assertFalse(hot.isGlobalOverlay());
        assertFalse(hot.isDisplayLegendIfNoFeaturesExist());
        assertTrue(hot.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("HOT Projects", hot.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", hot.getDescription());
        Assertions.assertNull(hot.getCategory());
        Assertions.assertEquals("layersInSelectedArea", hot.getGroup());
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

        assertFalse(urban.isGlobalOverlay());
        assertTrue(urban.isDisplayLegendIfNoFeaturesExist());
        assertTrue(urban.isBoundaryRequiredForRetrieval());
        //description is not populated for this layer (it's populated by LayerProvider and not changed by configs)
        Assertions.assertNull(urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
        assertFalse(urban.isEventIdRequiredForRetrieval());
    }

    @Test
    public void settledPeripheryTest() {
        Layer urban = Layer.builder()
                .id("kontur_settled_periphery")
                .build();
        service.applyConfig(urban);

        assertFalse(urban.isGlobalOverlay());
        assertTrue(urban.isDisplayLegendIfNoFeaturesExist());
        assertTrue(urban.isBoundaryRequiredForRetrieval());
        //description is not populated for this layer (it's populated by LayerProvider and not changed by configs)
        Assertions.assertNull(urban.getDescription());

        Assertions.assertNotNull(urban.getLegend());
        Assertions.assertEquals(SIMPLE, urban.getLegend().getType());
        assertFalse(urban.isEventIdRequiredForRetrieval());
    }

    @Test
    public void activeContributorsWithoutFeaturesTest() {
        Layer activeContributors = Layer.builder()
                .id("activeContributors")
                .source(LayerSource.builder().data(new FeatureCollection(null)).build())
                .build();
        service.applyConfig(activeContributors);
        //layer
        assertTrue(activeContributors.isGlobalOverlay());
        assertTrue(activeContributors.isDisplayLegendIfNoFeaturesExist());
        assertFalse(activeContributors.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Active contributors", activeContributors.getName());
        Assertions.assertEquals("other", activeContributors.getGroup());
        Assertions.assertEquals(OVERLAY, activeContributors.getCategory());
        //legend
        Assertions.assertNotNull(activeContributors.getLegend());
        Assertions.assertEquals(SIMPLE, activeContributors.getLegend().getType());
        Assertions.assertEquals("profile", activeContributors.getLegend().getLinkProperty()); //not used in this layer
        //steps
        //Steps are always shown since displayLegendIfNoFeaturesExist is true
        Assertions.assertEquals(3, activeContributors.getLegend().getSteps().size());
        Assertions.assertEquals("users", activeContributors.getLegend().getSteps().get(0).getSourceLayer());
        //skipping other fields
        assertFalse(activeContributors.isEventIdRequiredForRetrieval());
    }

    @Test
    public void bingTest() {
        Layer bing = Layer.builder().id("Bing").build();
        service.applyConfig(bing);
        Assertions.assertEquals(8, bing.getSource().getUrls().size());
        Assertions.assertEquals(BASE, bing.getCategory());
        Assertions.assertEquals("photo", bing.getGroup());
        assertFalse(bing.isEventIdRequiredForRetrieval());
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
        assertFalse(eventShape.isGlobalOverlay());
        assertTrue(eventShape.isDisplayLegendIfNoFeaturesExist());
        assertFalse(eventShape.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals("Event shape", eventShape.getName());
        Assertions.assertEquals("layersInSelectedArea", eventShape.getGroup());
        //legend
        Assertions.assertNotNull(eventShape.getLegend());
        Assertions.assertEquals(SIMPLE, eventShape.getLegend().getType());
        //steps
        Assertions.assertEquals(1, eventShape.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", eventShape.getLegend().getSteps().get(0).getStepName());
        //skipping other fields
        assertTrue(eventShape.isEventIdRequiredForRetrieval());
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
        assertFalse(layer.getLegend().getSteps().isEmpty());
        assertFalse(layer.isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", layer.getLegend().getSteps().get(0).getStepName());
        //skipping other fields
        assertTrue(layer.isEventIdRequiredForRetrieval());
    }

    @Test
    public void eventShapeCycloneAllStepsTest() {
        //event type = CYCLONE
        //features with different Class value exist - each should be added to legend
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(CYCLONE)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                //random order, some duplicates
                                feature("areaType", "centerPoint"),
                                feature("Class", "Poly_Red"),
                                feature("Class", "Poly_Green"),
                                feature("Class", "Poly_Orange"),
                                feature("Class", "Poly_Orange"),
                                feature("Class", "Poly_Green"),
                                feature("areaType", "position"),
                                feature("Class", "Poly_Green"),
                                feature("areaType", "track"),
                                feature("areaType", "alertArea")}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);
        assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(7, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Centroid", layer.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Exposure Area 60 km/h", layer.getLegend().getSteps().get(4).getStepName());
        Assertions.assertEquals("Exposure Area 90 km/h", layer.getLegend().getSteps().get(5).getStepName());
        Assertions.assertEquals("Exposure Area 120 km/h", layer.getLegend().getSteps().get(6).getStepName());
        Assertions.assertEquals("Line Track", layer.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("Point Track", layer.getLegend().getSteps().get(2).getStepName());
        Assertions.assertEquals("Uncertainty Cones", layer.getLegend().getSteps().get(3).getStepName());
        //skipping other fields
    }

    @Test
    public void eventShapeCycloneSomeStepsTest() {
        //event type = CYCLONE
        //features with different Class value exist - each should be added to legend
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(CYCLONE)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                //random order, some duplicates
                                feature("areaType", "centerPoint"),
                                feature("areaType", "track"),
                                feature("areaType", "position"),
                                feature("areaType", "alertArea")}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);
        assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(4, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Centroid", layer.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Line Track", layer.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("track", layer.getLegend().getSteps().get(1).getParamValue());
        Assertions.assertEquals("Point Track", layer.getLegend().getSteps().get(2).getStepName());
        Assertions.assertEquals("position", layer.getLegend().getSteps().get(2).getParamValue());
        Assertions.assertEquals("Uncertainty Cones", layer.getLegend().getSteps().get(3).getStepName());
        //skipping other fields
        assertTrue(layer.isEventIdRequiredForRetrieval());
    }

    @Test
    public void eventShapeFloodExposureStepTest() {
        //event type = CYCLONE
        //features with different Class value exist - each should be added to legend
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(FLOOD)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                //random order, some duplicates
                                feature("areaType", "position")}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);
        assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", layer.getLegend().getSteps().get(0).getStepName());
    }

    @Test
    public void eventShapeVolcanoStepsShouldNotBeDuplicated() {
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(VOLCANO)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                feature("Class", "Poly_Cones_0"),
                                feature("areaType", "exposure")}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Exposure Area", layer.getLegend().getSteps().get(0).getStepName());
    }

    @Test
    public void eventShapeCycloneStepsShouldNotBeDuplicated() {
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(CYCLONE)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                feature("areaType", "track"),
                                feature("Class", "Line_Line_0"),
                                feature("areaType", "track"),
                                feature("Class", "Line_Line_1")
                        }
                        ))
                        .build())
                .build();

        service.applyConfig(layer);

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Line Track", layer.getLegend().getSteps().get(0).getStepName());
    }
}
