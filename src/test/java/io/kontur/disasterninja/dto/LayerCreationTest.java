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
            new File("src/test/resources/io/kontur/disasterninja/client/layers/osmlayer.json"),
                FeatureCollectionGeoJSON.class)
            .getFeatures();
        List<Layer> result = Layer.fromOsmLayers(features);
        //todo other fields
        Layer layer1 = result.stream().filter(it -> "Bing".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Bing aerial imagery", layer1.getName());
        Assertions.assertEquals("Satellite and aerial imagery.", layer1.getDescription());
        Assertions.assertEquals("https://wiki.openstreetmap.org/wiki/Bing_Maps", layer1.getCopyright());
        Assertions.assertEquals(22, layer1.getMaxZoom());
        Assertions.assertEquals(1, layer1.getMinZoom());
    }

    @Test
    public void fromHotProjectLayersTest() throws IOException {
        List<FeatureGeoJSON> features = objectMapper.readValue(
            new File("src/test/resources/io/kontur/disasterninja/client/layers/hotProjects.json"),
                FeatureCollectionGeoJSON.class)
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
            new File("src/test/resources/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
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
