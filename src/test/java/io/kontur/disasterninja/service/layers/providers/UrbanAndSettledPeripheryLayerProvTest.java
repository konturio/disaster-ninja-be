package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.MultiPolygon;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.kontur.disasterninja.service.layers.providers.UrbanAndPeripheryLayerProvider.SETTLED_PERIPHERY_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.UrbanAndPeripheryLayerProvider.URBAN_CORE_LAYER_ID;
import static io.kontur.disasterninja.util.TestUtil.emptyParams;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class UrbanAndSettledPeripheryLayerProvTest extends LayerProvidersTest {

    @MockBean
    InsightsApiGraphqlClient insightsApiClient;

    @BeforeEach
    private void init() throws IOException {
        Mockito.when(insightsApiClient.humanitarianImpactQuery(any()))
                .thenReturn(CompletableFuture.completedFuture(objectMapper.readValue(
                        getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
                        FeatureCollection.class)));
    }

    @Test
    public void listOneLayerNotPresentTest() throws IOException, ExecutionException, InterruptedException {
        //#8516
        FeatureCollection fc = objectMapper.readValue(
                getClass().getResource("/io/kontur/disasterninja/client/layers/population.json"),
                FeatureCollection.class);
        fc.getFeatures()[1] = null; //remove one of layers

        Mockito.when(insightsApiClient.humanitarianImpactQuery(any()))
                .thenReturn(CompletableFuture.completedFuture(fc));
        List<Layer> layers = urbanAndPeripheryLayerProvider.obtainLayers(LayerSearchParams
                .builder().boundary(new Point(new double[]{1d, 2d})).build()).get();
        assertEquals(1, layers.size());
    }

    @Test
    public void listEmptyGeojsonTest() throws ExecutionException, InterruptedException {
        assertTrue(urbanAndPeripheryLayerProvider.obtainLayers(emptyParams()).get().isEmpty());
    }

    @Test
    public void getEmptyGeoJsonTest() {
        assertThrows(WebApplicationException.class, () -> {
            urbanAndPeripheryLayerProvider.obtainLayer(URBAN_CORE_LAYER_ID, emptyParams());
        });
        assertThrows(WebApplicationException.class, () -> {
            urbanAndPeripheryLayerProvider.obtainLayer(SETTLED_PERIPHERY_LAYER_ID, emptyParams());
        });
    }

    @Test
    public void listTest() throws IOException, ExecutionException, InterruptedException {
        List<Layer> results = urbanAndPeripheryLayerProvider.obtainLayers(LayerSearchParams
                .builder().boundary(new Point(new double[]{1d, 2d})).build()).get();
        Layer urbanCore = results.stream().filter(it -> URBAN_CORE_LAYER_ID.equals(it.getId())).findAny().get();
        Layer periphery = results.stream().filter(it -> SETTLED_PERIPHERY_LAYER_ID.equals(it.getId())).findAny().get();

        assertEquals("Kontur Urban Core", urbanCore.getName());
        assertEquals("Kontur Urban Core highlights most populated region affected. For this" +
                " event 102411536 people reside on 139417.01km?? (out of total 150665683 people on 1631751.6km??). This" +
                " area should have higher priority in humanitarian activities.", urbanCore.getDescription());
        assertNull(urbanCore.getGroup()); //defaults are set later by LayerConfigService

        assertNotNull(periphery);
        assertEquals("Kontur Settled Periphery", periphery.getName());
        assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows" +
                " a spread-out part of the population in the region. For this event it adds 48254147 people on" +
                " 1492334.59km?? on top of Kontur Urban Core.", periphery.getDescription());
        assertNull(periphery.getGroup()); //defaults are set later by LayerConfigService
    }

    @Test
    public void getPeripheryTest() throws IOException {
        Layer periphery = urbanAndPeripheryLayerProvider.obtainLayer(SETTLED_PERIPHERY_LAYER_ID, LayerSearchParams
                .builder().boundary(new Point(new double[]{1d, 2d})).build());

        assertNotNull(periphery);
        assertEquals("Kontur Settled Periphery", periphery.getName());
        assertEquals("Kontur Settled Periphery is complimentary to Kontur Urban Core and shows" +
                " a spread-out part of the population in the region. For this event it adds 48254147 people on" +
                " 1492334.59km?? on top of Kontur Urban Core.", periphery.getDescription());
        assertNull(periphery.getGroup()); //defaults are set later by LayerConfigService


        //source
        assertNotNull(periphery.getSource());
        assertTrue(periphery.getSource().getData().getFeatures()[0].getGeometry() instanceof MultiPolygon);
        assertEquals(122, ((MultiPolygon) periphery.getSource().getData().getFeatures()[0].getGeometry())
                .getCoordinates().length);

    }

    @Test
    public void getUrbanTest() throws IOException {
        Layer urban = urbanAndPeripheryLayerProvider.obtainLayer(URBAN_CORE_LAYER_ID, LayerSearchParams
                .builder().boundary(new Point(new double[]{1d, 2d})).build());

        assertNotNull(urban);
        assertEquals("Kontur Urban Core", urban.getName());
        assertEquals("Kontur Urban Core highlights most populated region affected. For this event 102411536" +
                " people reside on 139417.01km?? (out of total 150665683 people on 1631751.6km??). This area should have " +
                "higher priority in humanitarian activities.", urban.getDescription());
        assertNull(urban.getGroup()); //defaults are set later by LayerConfigService

        //source
        assertNotNull(urban.getSource());
        assertTrue(urban.getSource().getData().getFeatures()[0].getGeometry() instanceof MultiPolygon);
        assertEquals(164, ((MultiPolygon) urban.getSource().getData().getFeatures()[0].getGeometry())
                .getCoordinates().length);
    }
}
