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

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;

public class LayerCreationTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void fromOsmLayersTest() throws IOException {
        List<FeatureGeoJSON> features = objectMapper.readValue(
                new File("src/test/resources/io/kontur/disasterninja/client/layers/osmlayer.json"),
                FeatureCollectionGeoJSON.class)
            .getFeatures();
        List<Layer> result = Layer.fromOsmLayers(features);

        Layer layer1 = result.stream().filter(it -> "Bing".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Bing aerial imagery", layer1.getName());
        Assertions.assertEquals("Satellite and aerial imagery.", layer1.getDescription());
        Assertions.assertEquals(BASE, layer1.getCategory());
        //category is empty -- test in layer2
        //todo legend
        //copyright is empty -- test in layer2
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
