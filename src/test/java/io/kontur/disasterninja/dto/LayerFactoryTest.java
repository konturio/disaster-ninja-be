package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.LayerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;
import static io.kontur.disasterninja.domain.enums.LegendType.SIMPLE;

@SpringBootTest
public class LayerFactoryTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    LayerFactory layerFactory;

    @BeforeEach
    private void setup() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void fromOsmLayersTest() throws IOException {
        List<Feature> features = List.of(objectMapper.readValue(
                new File("src/test/resources/io/kontur/disasterninja/client/layers/osmlayer.json"),
                FeatureCollection.class)
            .getFeatures());
        List<Layer> result = layerFactory.fromOsmLayers(features);

        Layer layer1 = result.stream().filter(it -> "Bing".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Bing aerial imagery", layer1.getName());
        Assertions.assertEquals("Satellite and aerial imagery.", layer1.getDescription());
        Assertions.assertEquals(BASE, layer1.getCategory());
        Assertions.assertNull(layer1.getGroup()); // test in layer2
        Assertions.assertNull(layer1.getLegend());
        Assertions.assertNull(layer1.getCopyright()); // test in layer2
        Assertions.assertEquals(22, layer1.getMaxZoom());
        Assertions.assertEquals(1, layer1.getMinZoom());

        Layer layer2 = result.stream().filter(it -> "EOXAT2018CLOUDLESS".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Sentinel-2 cloudless - https://s2maps.eu by EOX IT Services GmbH" +
            " (Contains modified Copernicus Sentinel data 2017 & 2018)", layer2.getCopyright());
        Assertions.assertEquals("Photo", layer2.getGroup());

        Layer layer3 = result.stream().filter(it -> "OSM_Inspector-Addresses".equals(it.getId())).findAny().get();
        Assertions.assertEquals(OVERLAY, layer3.getCategory());
    }

    @Test
    public void fromHotProjectLayersTest() throws IOException {
        List<Feature> features = List.of(objectMapper.readValue(
                new File("src/test/resources/io/kontur/disasterninja/client/layers/hotProjects.json"),
                FeatureCollection.class)
            .getFeatures());
        Layer result = layerFactory.fromHotProjectLayers(features);
        Assertions.assertEquals("hotProjects", result.getId());

        Assertions.assertEquals("Hot Projects", result.getName());
        Assertions.assertEquals("Projects on HOT Tasking Manager, ongoing and historical", result.getDescription());
        Assertions.assertEquals(OVERLAY, result.getCategory());
        Assertions.assertEquals("Kontur", result.getGroup());
        //legend
        Assertions.assertEquals(SIMPLE, result.getLegend().getType());
        Assertions.assertEquals(2, result.getLegend().getSteps().size());
        Assertions.assertEquals("Active", result.getLegend().getSteps().get(0).getStepName());
        Assertions.assertEquals("link_to_icon", result.getLegend().getSteps().get(0).getStyle().get("icon"));
        Assertions.assertEquals("Archived", result.getLegend().getSteps().get(1).getStepName());
        //todo other fields
    }

    @Test
    public void fromUrbanCodeTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            new File("src/test/resources/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = layerFactory.fromUrbanCodeAndPeripheryLayer(featureCollection);
        Assertions.assertEquals(2, result.size());

        Layer urbanCore = result.stream().filter(it -> "kontur_urban_core".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Urban Core", urbanCore.getName());
        Assertions.assertEquals("Kontur Urban Core highlights most populated region affected. For this event " +
                "{{population}} people reside on {{areaKm2}}km² (out of total {{totalPopulation}} people on " +
                "{{totalAreaKm2}}km²). This area should have higher priority in humanitarian activities.",
            urbanCore.getDescription());
        Assertions.assertEquals("Layers in selected area", urbanCore.getGroup());
        //legend
        Assertions.assertEquals(SIMPLE, urbanCore.getLegend().getType());
        Assertions.assertEquals(HEX, urbanCore.getLegend().getSteps().get(0).getStepShape());
        Assertions.assertEquals("#FF7B00", urbanCore.getLegend().getSteps().get(0).getStyle().get("casing-color"));
        //todo other fields
    }

    @Test
    public void fromUrbanCodeAndPeripheryLayerTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            new File("src/test/resources/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = layerFactory.fromUrbanCodeAndPeripheryLayer(featureCollection);
        Assertions.assertEquals(2, result.size());

        Layer periphery = result.stream().filter(it -> "kontur_settled_periphery".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Settled Periphery", periphery.getName());
        Assertions.assertEquals("Layers in selected area", periphery.getGroup());
        //legend
        Assertions.assertEquals(SIMPLE, periphery.getLegend().getType());
        Assertions.assertEquals(HEX, periphery.getLegend().getSteps().get(0).getStepShape());
        Assertions.assertEquals("#2AD72A", periphery.getLegend().getSteps().get(0).getStyle().get("casing-color"));
        //todo other fields
    }
}
