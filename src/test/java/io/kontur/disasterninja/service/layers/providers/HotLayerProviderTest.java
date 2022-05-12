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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.HOT_LAYER_ID;
import static io.kontur.disasterninja.util.TestUtil.emptyParams;
import static io.kontur.disasterninja.util.TestUtil.paramsWithSomeBoundary;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class HotLayerProviderTest extends LayerProvidersTest {

    @MockBean
    KcApiClient kcApiClient;

    @BeforeEach
    public void init() throws IOException {
        Mockito.when(kcApiClient.getCollectionItemsByCentroidGeometry(any(), any())).thenReturn(
            List.of(objectMapper.readValue(
                    getClass().getResource("/io/kontur/disasterninja/client/layers/hotprojects.json"),
                    FeatureCollection.class)
                .getFeatures()));
    }


    @Test
    public void list_emptyGeojson() throws ExecutionException, InterruptedException {
        assertTrue(hotLayerProvider.obtainLayers(emptyParams()).get().isEmpty());
    }

    @Test
    public void get_emptyGeojson() {
        assertThrows(WebApplicationException.class, () -> {
            hotLayerProvider.obtainLayer(HOT_LAYER_ID, emptyParams());
        });
    }

    @Test
    public void list() throws ExecutionException, InterruptedException {
        List<Layer> results = hotLayerProvider.obtainLayers(paramsWithSomeBoundary()).get(); //filtering is done in kcApiClient so not testing it here
        assertEquals(1, results.size());
        Layer result = results.get(0);
        assertLayer(result);
    }

    @Test
    public void get() {
        Layer result = hotLayerProvider.obtainLayer(HOT_LAYER_ID, paramsWithSomeBoundary());
        assertLayer(result);
    }

    @Test
    public void listNoIntersection() throws ExecutionException, InterruptedException {
        Mockito.when(kcApiClient.getCollectionItemsByCentroidGeometry(any(), any())).thenReturn(List.of());
        assertTrue(hotLayerProvider.obtainLayers(paramsWithSomeBoundary()).get().isEmpty());
    }

    @Test
    public void getNoIntersection() {
        Mockito.when(kcApiClient.getCollectionItemsByCentroidGeometry(any(), any())).thenReturn(List.of());
        Layer result = hotLayerProvider.obtainLayer(HOT_LAYER_ID, paramsWithSomeBoundary());
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
        Assertions.assertEquals(100, result.getSource().getData().getFeatures()[0].getProperties()
            .get(PROJECT_ID));
        Assertions.assertEquals(HOT_PROJECTS_URL + 100, result.getSource().getData().getFeatures()[0].getProperties()
            .get(PROJECT_LINK));
    }
}
