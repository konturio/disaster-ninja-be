package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import k2layers.api.model.FeatureCollectionGeoJSON;
import k2layers.api.model.FeatureGeoJSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wololo.geojson.FeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LayerCreationTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fromOsmLayersTest() throws IOException {
        List<FeatureGeoJSON> features = objectMapper.readValue(
            new File("src/test/resources/layers/osmlayer.json"), FeatureCollectionGeoJSON.class)
            .getFeatures();
        List<Layer> result = Layer.fromOsmLayers(features);
        //todo other fields
        Layer layer1 = result.stream().filter(it -> "Aargau-AGIS-2014".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Aargau-AGIS-2014", layer1.getId());
        Assertions.assertEquals("Kanton Aargau 25cm (AGIS 2014)", layer1.getName());
        Assertions.assertEquals("This imagery is provided via a proxy operated by https://sosm.ch/",
            layer1.getDescription());
        Assertions.assertEquals("https://wiki.openstreetmap.org/wiki/Switzerland/AGIS", layer1.getCopyright());
        Assertions.assertEquals(19, layer1.getMaxZoom());
        Assertions.assertEquals(8, layer1.getMinZoom());
    }

    @Test
    public void fromHotProjectLayersTest() throws IOException {
        List<FeatureGeoJSON> features = objectMapper.readValue(
            new File("src/test/resources/layers/hotProjects.json"), FeatureCollectionGeoJSON.class)
            .getFeatures();
        Layer result = Layer.fromHotProjectLayers(features);
        Assertions.assertEquals("hotProjects", result.getId());
        Assertions.assertEquals("Hot Projects", result.getName());
        Assertions.assertEquals(LayerCategory.OVERLAY, result.getCategory());
        //todo other fields
    }

    @Test
    public void fromUrbanCodeAndPeripheryLayerTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            new File("src/test/resources/layers/population.json"), FeatureCollection.class);
        List<Layer> result = Layer.fromUrbanCodeAndPeripheryLayer(featureCollection);
        Assertions.assertEquals(2, result.size());

        Layer urbanCore = result.stream().filter(it -> "kontur_urban_core".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Urban Core", urbanCore.getName());
        //todo other fields

        Layer periphery = result.stream().filter(it -> "kontur_settled_periphery".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Settled Periphery", periphery.getName());
        //todo other fields
    }
}
