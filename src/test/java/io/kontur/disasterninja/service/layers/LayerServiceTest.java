package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.providers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class LayerServiceTest {

    @MockBean
    HotLayerProvider hotLayerProvider;
    @MockBean
    OsmLayerProvider osmLayerProvider;
    @MockBean
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;
    @MockBean
    EventShapeLayerProvider eventApiProvider;
    @MockBean
    BivariateLayerProvider bivariateLayerProvider;

    @Autowired
    LayerService layerService;

    @BeforeEach
    public void beforeEach() {
        when(hotLayerProvider.obtainLayers(any(), any())).thenReturn(new ArrayList<>());
        when(osmLayerProvider.obtainLayers(any(), any())).thenReturn(new ArrayList<>());
        when(urbanAndPeripheryLayerProvider.obtainLayers(any(), any())).thenReturn(new ArrayList<>());
        when(eventApiProvider.obtainLayers(any(), any())).thenReturn(new ArrayList<>());
        when(bivariateLayerProvider.obtainLayers(any(), any())).thenReturn(new ArrayList<>());
        //add other providers
    }

    @Test
    public void globalOverlaysListTest() {
        //all providers return nothing (= no features matched by geometry), so only global overlays should be returned
        List<Layer> layers = layerService.getList(new Point(new double[]{1, 2}), null);
        //check all layers with 'globalOverlay: true' are present
        Assertions.assertEquals(8, layers.size());
        Assertions.assertEquals("Kontur OpenStreetMap Quantity", layers.get(0).getId());
        Assertions.assertEquals("Kontur OpenStreetMap Building Quantity", layers.get(1).getId());
        Assertions.assertEquals("Kontur OpenStreetMap Road Length", layers.get(2).getId());
        Assertions.assertEquals("Kontur OpenStreetMap Mapping Activity", layers.get(3).getId());
        Assertions.assertEquals("Kontur OpenStreetMap Antiquity", layers.get(4).getId());
        Assertions.assertEquals("Kontur Nighttime Heatwave Risk", layers.get(5).getId());
        System.out.println(layers);
    }

    @Test
    public void globalOverlaysGetTest() {
        //all providers return nothing (= no features matched by geometry), so global overlay should be returned
        List<Layer> layers = layerService.get(new Point(new double[]{1, 2}), List.of("Kontur Nighttime Heatwave Risk"),
            null);
        Assertions.assertEquals(1, layers.size());
        System.out.println(layers);
    }
}
