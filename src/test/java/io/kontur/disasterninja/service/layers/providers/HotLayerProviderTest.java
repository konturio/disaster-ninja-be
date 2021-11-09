package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.service.layers.providers.LayerProvider.HOT_LAYER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class HotLayerProviderTest extends LayerProvidersTest {

    @MockBean
    KcApiClient kcApiClient;

    @BeforeEach
    public void init() throws IOException {
        Mockito.when(kcApiClient.getCollectionItemsByGeometry(any(), any())).thenReturn(
            List.of(objectMapper.readValue(
                    getClass().getResource("/io/kontur/disasterninja/client/layers/hotprojects.json"),
                    FeatureCollection.class)
                .getFeatures()));
    }


    @Test
    public void list_emptyGeojson() {
        List<Layer> result = hotLayerProvider.obtainLayers(null, UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    public void get_emptyGeojson() {
        assertThrows(WebApplicationException.class, () -> {
            hotLayerProvider.obtainLayer(null, HOT_LAYER_ID, UUID.randomUUID());
        });
    }

    @Test
    public void list() {
        List<Layer> results = hotLayerProvider.obtainLayers(new Point(new double[]{16.428510034, 8.230779546}), null); //filtering is done in kcApiClient so not testing it here
        assertEquals(1, results.size());
        Layer result = results.get(0);
        assertLayer(result);
    }

    @Test
    public void get() {
        Layer result = hotLayerProvider.obtainLayer(new Point(new double[]{16.428510034, 8.230779546}), HOT_LAYER_ID, null);
        assertLayer(result);
    }

    @Test
    public void listNoIntersection() {
        Mockito.when(kcApiClient.getCollectionItemsByGeometry(any(), any())).thenReturn(List.of());
        List<Layer> results = hotLayerProvider.obtainLayers(new Point(new double[]{-90d, -90d}), null);
        assertEquals(0, results.size());
    }

    @Test
    public void getNoIntersection() {
        Mockito.when(kcApiClient.getCollectionItemsByGeometry(any(), any())).thenReturn(List.of());
        Layer result = hotLayerProvider.obtainLayer(new Point(new double[]{10d, 20d}), HOT_LAYER_ID, null);
        assertNull(result);
    }

    private void assertLayer(Layer result) {
        Assertions.assertEquals("hotProjects", result.getId());
        Assertions.assertNotNull(result.getSource());
        Assertions.assertEquals(10, result.getSource().getData().getFeatures().length);
        //some random features fields
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[0].getGeometry()
            .getType());
        Assertions.assertNull(result.getDescription()); //defaults are set later by LayerConfigService
    }
}
