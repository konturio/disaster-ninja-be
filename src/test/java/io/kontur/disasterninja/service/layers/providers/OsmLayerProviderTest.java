package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class OsmLayerProviderTest extends LayerProvidersTest {

    @MockBean
    KcApiClient kcApiClient;

    @BeforeEach
    private void init() throws IOException {
        //list
        Mockito.when(kcApiClient.getCollectionItemsByGeometry(any(), any())).thenReturn(
            List.of(objectMapper.readValue(
                    getClass().getResource("/io/kontur/disasterninja/client/layers/osmlayer.json"),
                    FeatureCollection.class)
                .getFeatures())
        );
        //single
        Mockito.when(kcApiClient.getFeatureFromCollection(any(), any(), any())).thenReturn(
            objectMapper.readValue(getClass().getResource("/io/kontur/disasterninja/client/layers/osmlayer_feature.json"),
                Feature.class)
        );
    }

    @Test
    public void list_emptyGeoJson() {
        //no geojson => no result
        Assertions.assertTrue(osmLayerProvider.obtainLayers(null, UUID.randomUUID()).isEmpty());
    }

    @Test
    public void get_emptyGeoJson() {
        //no geoJson => no filtering is applied
        Layer layer = osmLayerProvider.obtainLayer(null, "Benin_cotonou_pleiade_2016", null);
        assertEquals("Benin_cotonou_pleiade_2016", layer.getId());
        //source
        assertTrue(layer.getSource().getData().getFeatures().length > 0);
    }

    @Test
    public void list() {
        Geometry point = new Point(new double[]{1.722946974, 6.266307793});
        List<Layer> result = osmLayerProvider.obtainLayers(point, null);

        assertEquals(10, result.size()); //9 layers without geometry, 1 matching

        Layer bygeom = result.stream().filter(it -> "Benin_cotonou_pleiade_2016".equals(it.getId()))
            .findAny().get();
        assertNotNull(bygeom);

        //feature without geometry is included
        Layer layer1 = result.stream().filter(it -> "Bing".equals(it.getId())).findAny().get();
        assertEquals("Bing aerial imagery", layer1.getName());
        assertEquals("Satellite and aerial imagery.", layer1.getDescription());
        assertEquals(BASE, layer1.getCategory());
        Assertions.assertNull(layer1.getGroup()); // test in layer2
        Assertions.assertNull(layer1.getLegend());
        Assertions.assertNull(layer1.getCopyrights()); // test in layer2
        assertEquals(22, layer1.getMaxZoom());
        assertEquals(1, layer1.getMinZoom());

        Layer layer2 = result.stream().filter(it -> "EOXAT2018CLOUDLESS".equals(it.getId())).findAny().get();
        assertEquals("Sentinel-2 cloudless - https://s2maps.eu by EOX IT Services GmbH" +
            " (Contains modified Copernicus Sentinel data 2017 & 2018)", layer2.getCopyrights().get(0));
        assertEquals("Photo", layer2.getGroup());

        Layer layer3 = result.stream().filter(it -> "OSM_Inspector-Addresses".equals(it.getId())).findAny().get();
        assertEquals(OVERLAY, layer3.getCategory());
    }

    @Test
    public void get() {
        Layer layer1 = osmLayerProvider.obtainLayer(new Point(new double[]{1.722946974, 6.266307793}),
            "Benin_cotonou_pleiade_2016", null);

        assertEquals("Benin: Cotonou Pleiade 2016", layer1.getName());
        assertEquals(BASE, layer1.getCategory());
        assertEquals("Photo", layer1.getGroup()); // test in layer2
        Assertions.assertNull(layer1.getLegend());
        Assertions.assertNull(layer1.getCopyrights());
        assertEquals(21, layer1.getMaxZoom());
        assertEquals(6, layer1.getMinZoom());
        //source
        assertNotNull(layer1.getSource().getData().getFeatures());
        assertTrue(layer1.getSource().getData().getFeatures()[0].getGeometry() instanceof MultiPolygon);
        assertEquals(51, ((MultiPolygon) layer1.getSource().getData().getFeatures()[0].getGeometry())
            .getCoordinates()[0][0].length);
    }
}