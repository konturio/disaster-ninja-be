package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.EventDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.dto.EventType.EARTHQUAKE;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.EVENT_SHAPE_LAYER_ID;

@SpringBootTest
public class LayerProvidersTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    OsmLayerProvider osmLayerProvider;

    @Autowired
    HotLayerProvider hotLayerProvider;

    @Autowired
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;

    @Autowired
    EventShapeLayerProvider eventShapeLayerProvider;

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
        List<Layer> result = osmLayerProvider.fromOsmLayers(features);

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
                new File("src/test/resources/io/kontur/disasterninja/client/layers/hotprojects.json"),
                FeatureCollection.class)
            .getFeatures());
        Layer result = hotLayerProvider.fromHotProjectLayers(features);
        Assertions.assertEquals("hotProjects", result.getId());

        Assertions.assertNull(result.getName()); //defaults are set later by LayerConfigService
        //todo other fields
    }

    @Test
    public void fromUrbanCoreTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            new File("src/test/resources/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = urbanAndPeripheryLayerProvider.fromUrbanCodeAndPeripheryLayer(featureCollection);
        Assertions.assertEquals(2, result.size());

        Layer urbanCore = result.stream().filter(it -> "kontur_urban_core".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Urban Core", urbanCore.getName());
        Assertions.assertNull(urbanCore.getDescription()); //defaults are set later by LayerConfigService
        //todo other fields
    }

    @Test
    public void fromUrbanCoreAndPeripheryLayerTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            new File("src/test/resources/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = urbanAndPeripheryLayerProvider.fromUrbanCodeAndPeripheryLayer(featureCollection);
        Assertions.assertEquals(2, result.size());

        Layer periphery = result.stream().filter(it -> "kontur_settled_periphery".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Settled Periphery", periphery.getName());
        Assertions.assertNull(periphery.getDescription()); //defaults are set later by LayerConfigService
        //todo other fields
    }

    @Test
    public void eventShapeEarthquakeLayerTest() throws IOException {
        EventDto eventDto = objectMapper.readValue(new File(ClassLoader
                .getSystemResource("io/kontur/disasterninja/client/layers/eventdto.json").getPath()),
            EventDto.class);
        Layer result = eventShapeLayerProvider.fromEventDto(eventDto);
        //there are "Class" properties in features - which define further layer id hence layer config
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID + "." + EARTHQUAKE, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void eventShapeDefaultLayerTest() throws IOException {
        EventDto eventDto = objectMapper.readValue(new File(ClassLoader
                .getSystemResource("io/kontur/disasterninja/client/layers/eventdto.json").getPath()),
            EventDto.class);
        //remove "Class" entries from features properties
        Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures()).forEach(feature -> {
            if (feature.getProperties() != null) {
                feature.getProperties().remove("Class");
            }
        });
        Layer result = eventShapeLayerProvider.fromEventDto(eventDto);
        //"Class" property is absent from features properties => the default template is used
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }
}
