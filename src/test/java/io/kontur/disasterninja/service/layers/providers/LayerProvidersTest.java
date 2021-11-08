package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.VECTOR;
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

    @Autowired
    BivariateLayerProvider bivariateLayerProvider;

    @BeforeEach
    private void setup() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void fromOsmLayersTest() throws IOException {
        List<Feature> features = List.of(objectMapper.readValue(
                getClass().getResource("/io/kontur/disasterninja/client/layers/osmlayer.json"),
                FeatureCollection.class)
            .getFeatures());
        List<Layer> result = osmLayerProvider.fromOsmLayers(features);

        Layer layer1 = result.stream().filter(it -> "Bing".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Bing aerial imagery", layer1.getName());
        Assertions.assertEquals("Satellite and aerial imagery.", layer1.getDescription());
        Assertions.assertEquals(BASE, layer1.getCategory());
        Assertions.assertNull(layer1.getGroup()); // test in layer2
        Assertions.assertNull(layer1.getLegend());
        Assertions.assertNull(layer1.getCopyrights()); // test in layer2
        Assertions.assertEquals(22, layer1.getMaxZoom());
        Assertions.assertEquals(1, layer1.getMinZoom());

        Layer layer2 = result.stream().filter(it -> "EOXAT2018CLOUDLESS".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Sentinel-2 cloudless - https://s2maps.eu by EOX IT Services GmbH" +
            " (Contains modified Copernicus Sentinel data 2017 & 2018)", layer2.getCopyrights().get(0));
        Assertions.assertEquals("Photo", layer2.getGroup());

        Layer layer3 = result.stream().filter(it -> "OSM_Inspector-Addresses".equals(it.getId())).findAny().get();
        Assertions.assertEquals(OVERLAY, layer3.getCategory());
    }

    @Test
    public void fromOsmLayerDetailsTest() throws IOException {
        Feature feature = objectMapper.readValue(
            getClass().getResource("/io/kontur/disasterninja/client/layers/osmlayer_feature.json"),
            Feature.class);
        Layer layer1 = osmLayerProvider.fromOsmLayer(feature, true);

        Assertions.assertEquals("Benin: Cotonou Pleiade 2016", layer1.getName());
        Assertions.assertEquals(BASE, layer1.getCategory());
        Assertions.assertEquals("Photo", layer1.getGroup()); // test in layer2
        Assertions.assertNull(layer1.getLegend());
        Assertions.assertNull(layer1.getCopyrights());
        Assertions.assertEquals(21, layer1.getMaxZoom());
        Assertions.assertEquals(6, layer1.getMinZoom());
        //source
        Assertions.assertEquals(feature, layer1.getSource().getData().getFeatures()[0]);
    }

    @Test
    public void fromHotProjectLayersTest() throws IOException {
        List<Feature> features = List.of(objectMapper.readValue(
                getClass().getResource("/io/kontur/disasterninja/client/layers/hotprojects.json"),
                FeatureCollection.class)
            .getFeatures());
        Layer result = hotLayerProvider.fromHotProjectLayers(features, true);
        Assertions.assertEquals("hotProjects", result.getId());
        Assertions.assertNotNull(result.getSource());
        Assertions.assertEquals(10, result.getSource().getData().getFeatures().length);
        //some random features fields
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[0].getGeometry()
            .getType());
        Assertions.assertEquals("PierZen", result.getSource().getData().getFeatures()[9]
            .getProperties().get("author"));
        Assertions.assertNull(result.getDescription()); //defaults are set later by LayerConfigService
    }

    @Test
    public void fromUrbanCoreTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = urbanAndPeripheryLayerProvider.fromUrbanCoreAndPeripheryLayer(featureCollection, false);
        Assertions.assertEquals(2, result.size());

        Layer urbanCore = result.stream().filter(it -> "kontur_urban_core".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Urban Core", urbanCore.getName());
        Assertions.assertEquals("Kontur Urban Core highlights most populated region affected. For this" +
            " event 102411536 people reside on 139417.01km² (out of total 150665683 people on 1631751.6km²). This" +
            " area should have higher priority in humanitarian activities.", urbanCore.getDescription());
        Assertions.assertNull(urbanCore.getGroup()); //defaults are set later by LayerConfigService
    }

    @Test
    public void fromUrbanCoreAndPeripheryLayerTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = urbanAndPeripheryLayerProvider.fromUrbanCoreAndPeripheryLayer(featureCollection, false);
        Assertions.assertEquals(2, result.size());

        Layer periphery = result.stream().filter(it -> "kontur_settled_periphery".equals(it.getId())).findAny().get();
        Assertions.assertEquals("Kontur Settled Periphery", periphery.getName());
        Assertions.assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows" +
            " a spread-out part of the population in the region. For this event it adds 48254147 people on" +
            " 1492334.59km² on top of Kontur Urban Core.", periphery.getDescription());
        Assertions.assertNull(periphery.getGroup()); //defaults are set later by LayerConfigService
    }

    @Test
    public void fromUrbanCoreAndPeripheryLayerSourceTest() throws IOException {
        FeatureCollection featureCollection = objectMapper.readValue(
            getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
            FeatureCollection.class);
        List<Layer> result = urbanAndPeripheryLayerProvider.fromUrbanCoreAndPeripheryLayer(featureCollection, true);
        Assertions.assertEquals(2, result.size());

        Feature feature = Arrays.stream(featureCollection.getFeatures()).filter(it -> "kontur_settled_periphery"
            .equals(it.getId())).findFirst().get();

        Layer periphery = result.stream().filter(it -> "kontur_settled_periphery".equals(it.getId())).findAny().get();
        Assertions.assertNotNull(periphery.getSource());
        Assertions.assertEquals(periphery.getSource().getData().getFeatures()[0], periphery.getSource().getData()
            .getFeatures()[0]);
    }

    @Test
    public void eventShapeEarthquakeLayerTest() throws IOException {
        EventDto eventDto = objectMapper.readValue(getClass()
                .getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
            EventDto.class);
        Layer result = eventShapeLayerProvider.fromEventDto(eventDto, true);
        //there are "Class" properties in features - which define further layer id hence layer config
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID + "." + EARTHQUAKE, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void eventShapeDefaultLayerTest() throws IOException {
        EventDto eventDto = objectMapper.readValue(getClass()
                .getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
            EventDto.class);
        //remove "Class" entries from features properties
        Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures()).forEach(feature -> {
            if (feature.getProperties() != null) {
                feature.getProperties().remove("Class");
            }
        });
        Layer result = eventShapeLayerProvider.fromEventDto(eventDto, true);
        //"Class" property is absent from features properties => the default template is used
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void eventShapeDefaultWithoutSourceTest() throws IOException {
        EventDto eventDto = objectMapper.readValue(getClass()
                .getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
            EventDto.class);
        //remove "Class" entries from features properties
        Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures()).forEach(feature -> {
            if (feature.getProperties() != null) {
                feature.getProperties().remove("Class");
            }
        });
        Layer result = eventShapeLayerProvider.fromEventDto(eventDto, false);
        //"Class" property is absent from features properties => the default template is used
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID, result.getId());
        //check source data was not loaded
        Assertions.assertNull(result.getSource());
    }

    @Test
    public void bivariateLayerProviderTest() {
        BivariateLayerLegendQuery.Overlay overlay = new BivariateLayerLegendQuery
            .Overlay("Overlay", "Kontur OpenStreetMap Quantity",
            "This map shows relative distribution of OpenStreetMap objects and Population. Last updated" +
                " 2021-11-06T20:59:29Z",
            new BivariateLayerLegendQuery.X("Axis", "OSM objects (n/km²)", steps(0d, 1d, 2d, 1000d),
                List.of("count", "area_km2")),
            new BivariateLayerLegendQuery.Y("Axis", "Population (ppl/km²)", steps1(0d, 10d, 20d, 10000d),
                List.of("count", "area_km2")),
            List.of(new BivariateLayerLegendQuery.Color("OverlayColor", "A1", "rgb(111,232,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "A2", "rgb(222,232,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "A3", "rgb(333,232,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "B1", "rgb(232,111,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "B2", "rgb(232,222,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "B3", "rgb(232,333,157)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "C1", "rgb(232,232,111)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "C2", "rgb(232,232,222)"),
                new BivariateLayerLegendQuery.Color("OverlayColor", "C3", "rgb(232,232,333)"))
        );

        Layer biv = bivariateLayerProvider.fromOverlay(overlay);

        //layer
        Assertions.assertEquals("Kontur OpenStreetMap Quantity", biv.getId());
        Assertions.assertEquals("This map shows relative distribution of OpenStreetMap objects and Population." +
            " Last updated 2021-11-06T20:59:29Z", biv.getDescription());

        //source
        Assertions.assertEquals(VECTOR, biv.getSource().getType());

        //legend
        Assertions.assertNotNull(biv.getLegend());
        Assertions.assertEquals(8, biv.getLegend().getSteps().size());
        //step1
        LegendStep xStep1 = biv.getLegend().getSteps().get(0);
        Assertions.assertEquals(0, xStep1.getOrder());
        Assertions.assertNull(xStep1.getParamName()); //axis is used instead
        Assertions.assertNull(xStep1.getParamValue()); //axisValue is used instead
        Assertions.assertEquals("X", xStep1.getAxis());
        Assertions.assertEquals(0.0d, xStep1.getAxisValue());
        Assertions.assertNull(xStep1.getStepName());
        Assertions.assertNull(xStep1.getStepShape());
        Assertions.assertNull(xStep1.getStyle());
        //last step for Y axis
        LegendStep yStep = biv.getLegend().getSteps().get(7);
        Assertions.assertEquals("Y", yStep.getAxis());
        Assertions.assertEquals(10000d, yStep.getAxisValue());
        //skipping other params

        //colors
        Assertions.assertNotNull(biv.getLegend().getBivariateColors());
        Assertions.assertEquals(9, biv.getLegend().getBivariateColors().entrySet().size());

        String a1 = biv.getLegend().getBivariateColors().get("A1");
        Assertions.assertEquals("rgb(111,232,157)", a1);
        String c3 = biv.getLegend().getBivariateColors().get("C3");
        Assertions.assertEquals("rgb(232,232,333)", c3);
    }

    private List<BivariateLayerLegendQuery.Step> steps(double d1, double d2, double d3, double d4) {
        BivariateLayerLegendQuery.Step s1 = new BivariateLayerLegendQuery.Step("Step", null, d1);
        BivariateLayerLegendQuery.Step s2 = new BivariateLayerLegendQuery.Step("Step", null, d2);
        BivariateLayerLegendQuery.Step s3 = new BivariateLayerLegendQuery.Step("Step", null, d3);
        BivariateLayerLegendQuery.Step s4 = new BivariateLayerLegendQuery.Step("Step", null, d4);
        return List.of(s1, s2, s3, s4);
    }

    private List<BivariateLayerLegendQuery.Step1> steps1(double d1, double d2, double d3, double d4) {
        BivariateLayerLegendQuery.Step1 s1 = new BivariateLayerLegendQuery.Step1("Step", null, d1);
        BivariateLayerLegendQuery.Step1 s2 = new BivariateLayerLegendQuery.Step1("Step", null, d2);
        BivariateLayerLegendQuery.Step1 s3 = new BivariateLayerLegendQuery.Step1("Step", null, d3);
        BivariateLayerLegendQuery.Step1 s4 = new BivariateLayerLegendQuery.Step1("Step", null, d4);
        return List.of(s1, s2, s3, s4);
    }
}
