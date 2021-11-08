package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.providers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public void globalOverlaysTest() {
        //all providers return nothing (= no features matched by geometry), so only global overlays should be returned
        List<Layer> layers = layerService.getList(new Point(new double[]{1, 2}), null);
        //check all layers with 'globalOverlay: true' are present
        Assertions.assertEquals(10, layers.size());
        System.out.println(layers);
    }

    @Test
    public void geojsonGeometryTest() {
        FeatureCollection fc = new FeatureCollection(new Feature[]{
            new Feature(new Point(new double[]{1, 0}), new HashMap<>()),
            new Feature(new Point(new double[]{2, 0}), new HashMap<>())});

        Geometry result = LayerService.getGeometryFromGeoJson(fc);
        Assertions.assertEquals(Arrays.stream(fc.getFeatures()).map(Feature::getGeometry).collect(Collectors.toList()),
            Arrays.stream(((GeometryCollection) result).getGeometries()).toList());

        Geometry geometry = new Point(new double[]{1, 0});
        result = LayerService.getGeometryFromGeoJson(geometry);
        Assertions.assertEquals(geometry, result);

        Feature f = new Feature(new Point(
            new double[]{1, 0}), new HashMap<>());
        result = LayerService.getGeometryFromGeoJson(f);
        Assertions.assertEquals(f.getGeometry(), result);
    }
}
