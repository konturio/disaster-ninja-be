package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.MultiPolygon;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.service.layers.providers.LayerProvider.SETTL_PERIPHERY_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.URBAN_CORE_LAYER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class UrbanAndSettledPeripheryLayerProvTest extends LayerProvidersTest {

    @MockBean
    InsightsApiClient insightsApiClient;

    @BeforeEach
    private void init() throws IOException {
        Mockito.when(insightsApiClient.getUrbanCoreAndSettledPeripheryLayers(any()))
            .thenReturn(objectMapper.readValue(
                getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
                FeatureCollection.class));
    }

    @Test
    public void list_emptyGeojson() {
        assertNull(urbanAndPeripheryLayerProvider.obtainLayers(null, UUID.randomUUID()));
    }

    @Test
    public void get_emptyGeoJson() {
        assertThrows(WebApplicationException.class, () -> {
            urbanAndPeripheryLayerProvider.obtainLayer(null, URBAN_CORE_LAYER_ID, UUID.randomUUID());
        });
        assertThrows(WebApplicationException.class, () -> {
            urbanAndPeripheryLayerProvider.obtainLayer(null, SETTL_PERIPHERY_LAYER_ID, UUID.randomUUID());
        });
    }

    @Test
    public void list() throws IOException {
        List<Layer> results = urbanAndPeripheryLayerProvider.obtainLayers(new Point(new double[]{0d, 0d}), null);
        Layer urbanCore = results.stream().filter(it -> URBAN_CORE_LAYER_ID.equals(it.getId())).findAny().get();
        Layer periphery = results.stream().filter(it -> SETTL_PERIPHERY_LAYER_ID.equals(it.getId())).findAny().get();

        assertEquals("Kontur Urban Core", urbanCore.getName());
        assertEquals("Kontur Urban Core highlights most populated region affected. For this" +
            " event 102411536 people reside on 139417.01km² (out of total 150665683 people on 1631751.6km²). This" +
            " area should have higher priority in humanitarian activities.", urbanCore.getDescription());
        assertNull(urbanCore.getGroup()); //defaults are set later by LayerConfigService

        assertNotNull(periphery);
        assertEquals("Kontur Settled Periphery", periphery.getName());
        assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows" +
            " a spread-out part of the population in the region. For this event it adds 48254147 people on" +
            " 1492334.59km² on top of Kontur Urban Core.", periphery.getDescription());
        assertNull(periphery.getGroup()); //defaults are set later by LayerConfigService
    }

    @Test
    public void get_Periphery() throws IOException {
        Layer periphery = urbanAndPeripheryLayerProvider.obtainLayer(new Point(new double[]{-0.096666164, 6.286422267})
            , SETTL_PERIPHERY_LAYER_ID, null);

        assertNotNull(periphery);
        assertEquals("Kontur Settled Periphery", periphery.getName());
        assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows" +
            " a spread-out part of the population in the region. For this event it adds 48254147 people on" +
            " 1492334.59km² on top of Kontur Urban Core.", periphery.getDescription());
        assertNull(periphery.getGroup()); //defaults are set later by LayerConfigService


        //source
        assertNotNull(periphery.getSource());
        assertTrue(periphery.getSource().getData().getFeatures()[0].getGeometry() instanceof MultiPolygon);
        assertEquals(122, ((MultiPolygon) periphery.getSource().getData().getFeatures()[0].getGeometry())
            .getCoordinates().length);

    }

    @Test
    public void get_Urban() throws IOException {
        Layer urban = urbanAndPeripheryLayerProvider.obtainLayer(new Point(new double[]{2.512246911, 49.914386015})
            , URBAN_CORE_LAYER_ID, null);

        assertNotNull(urban);
        assertEquals("Kontur Urban Core", urban.getName());
        assertEquals("Kontur Urban Core highlights most populated region affected. For this event 102411536" +
            " people reside on 139417.01km² (out of total 150665683 people on 1631751.6km²). This area should have " +
            "higher priority in humanitarian activities.", urban.getDescription());
        assertNull(urban.getGroup()); //defaults are set later by LayerConfigService

        //source
        assertNotNull(urban.getSource());
        assertTrue(urban.getSource().getData().getFeatures()[0].getGeometry() instanceof MultiPolygon);
        assertEquals(164, ((MultiPolygon) urban.getSource().getData().getFeatures()[0].getGeometry())
            .getCoordinates().length);
    }
}
