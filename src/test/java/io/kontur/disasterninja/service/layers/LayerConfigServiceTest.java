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

import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;
import static io.kontur.disasterninja.dto.EventType.*;
import static io.kontur.disasterninja.service.layers.providers.EventShapeLayerProvider.EVENT_SHAPE_LAYER_ID;
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
                                feature("Class", "Poly_Cones")}
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
                                feature("Class", "Poly_Cones")}
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
    public void eventShapeVolcanoAllStepsTest() {
        //event type = VOLCANO
        //features with different areaType and forecastHrs value exist - each should be added to legend
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(VOLCANO)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                feature("areaType", "centerPoint"),
                                feature("forecastHrs", 0),
                                feature("forecastHrs", 6),
                                feature("forecastHrs", 12),
                                feature("areaType", "alertArea"),
                                feature("forecastHrs", 18)}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);
        assertFalse(layer.isBoundaryRequiredForRetrieval());

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(6, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Centroid", layer.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("Exposure Area 100 km", layer.getLegend().getSteps().get(1).getStepName());
        Assertions.assertEquals("Initial Forecast", layer.getLegend().getSteps().get(2).getStepName());
        Assertions.assertEquals("6 hours Forecast", layer.getLegend().getSteps().get(3).getStepName());
        Assertions.assertEquals("12 hours Forecast", layer.getLegend().getSteps().get(4).getStepName());
        Assertions.assertEquals("18 hours Forecast", layer.getLegend().getSteps().get(5).getStepName());
    }

    @Test
    public void eventShapeVolcanoStepsShouldNotBeDuplicated() {
        Layer layer = Layer.builder()
                .id(EVENT_SHAPE_LAYER_ID)
                .eventType(VOLCANO)
                .source(LayerSource.builder()
                        .data(new FeatureCollection(new Feature[]{
                                feature("Class", "Poly_Cones_0"),
                                feature("forecastHrs", 0)}
                        ))
                        .build())
                .build();

        service.applyConfig(layer);

        Assertions.assertNotNull(layer.getLegend());
        assertFalse(layer.getLegend().getSteps().isEmpty());
        Assertions.assertEquals(SIMPLE, layer.getLegend().getType());
        Assertions.assertEquals(1, layer.getLegend().getSteps().size());
        Assertions.assertEquals("Initial Forecast", layer.getLegend().getSteps().get(0).getStepName());
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
