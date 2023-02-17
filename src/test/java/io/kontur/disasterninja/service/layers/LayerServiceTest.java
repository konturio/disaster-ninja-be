package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.service.layers.providers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.service.layers.providers.UrbanAndPeripheryLayerProvider.SETTLED_PERIPHERY_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.UrbanAndPeripheryLayerProvider.URBAN_CORE_LAYER_ID;
import static io.kontur.disasterninja.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LayerServiceTest {

    final String id = "myId";
    final String title = "layer title";
    final Legend legend = createLegend();
    final Layer layer = Layer.builder()
            .id(id)
            .name(title)
            .legend(legend)
            .build();

    final Layer userLayer = Layer.builder().id("userLayer").build();
    final Layer commonLayer = Layer.builder().id("commonLayer").build();

    @MockBean
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;
    @MockBean
    EventShapeLayerProvider eventApiProvider;
    @MockBean
    BivariateLayerProvider bivariateLayerProvider;
    @MockBean
    LayersApiClient layersApiClient;
    @MockBean
    LayersApiProvider layersApiProvider;

    @Autowired
    LayerService layerService;

    @BeforeEach
    public void beforeEach() {
        when(urbanAndPeripheryLayerProvider.obtainGlobalLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(eventApiProvider.obtainGlobalLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(bivariateLayerProvider.obtainGlobalLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(layersApiProvider.obtainGlobalLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));

        when(urbanAndPeripheryLayerProvider.obtainUserLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(eventApiProvider.obtainUserLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(bivariateLayerProvider.obtainUserLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(layersApiProvider.obtainUserLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));

        when(urbanAndPeripheryLayerProvider.obtainSelectedAreaLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(eventApiProvider.obtainSelectedAreaLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(bivariateLayerProvider.obtainSelectedAreaLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));
        when(layersApiProvider.obtainSelectedAreaLayers(any())).thenReturn(
                CompletableFuture.completedFuture(new ArrayList<>()));

        //add other providers
    }

    @Test
    public void useBoundaryFilterOrNotTest() {
        givenUserLayerIsReturnedOnlyWhenQueriedWithoutGeometry();
        givenCommonLayerIsReturnedOnlyWhenQueriedWithGeometry();

        List<Layer> result = layerService.get(List.of(commonLayer.getId()), List.of(userLayer.getId()),
                paramsWithSomeBoundary());
        assertEquals(2, result.size());
        assertTrue(result.contains(userLayer));
        assertTrue(result.contains(commonLayer));
    }

    @Test
    public void useBoundaryFilterOrNotNegativeTest() {
        givenUserLayerIsReturnedOnlyWhenQueriedWithoutGeometry();
        givenCommonLayerIsReturnedOnlyWhenQueriedWithGeometry();

        try {
            layerService.get(List.of(userLayer.getId()), List.of(commonLayer.getId()),
                    paramsWithSomeBoundary());
            throw new RuntimeException("Expected exception was not thrown!");
        } catch (WebApplicationException e) {
            assertEquals("Layer not found / no layer data found by id and boundary!", e.getMessage());
        }
    }

    @Test
    public void noDuplicatesShouldBePresentInResponseTest() {
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(any(), any())).thenReturn(userLayer);

        List<Layer> result = layerService.get(List.of(userLayer.getId()), List.of(userLayer.getId()),
                paramsWithSomeBoundary());
        verify(layersApiProvider, times(1)).obtainLayer(eq(userLayer.getId()),
                argThat(p -> p.getBoundary() != null));
        verify(layersApiProvider, times(1)).obtainLayer(eq(userLayer.getId()),
                argThat(p -> p.getBoundary() == null));
        assertEquals(1, result.size());
    }

    @Test
    public void eventShapeLayersAreProcessedByEventShapeProviderTest() {
        when(layersApiProvider.isApplicable(any())).thenCallRealMethod();
        when(eventApiProvider.isApplicable(any())).thenCallRealMethod();
        when(urbanAndPeripheryLayerProvider.isApplicable(any())).thenCallRealMethod();
        when(bivariateLayerProvider.isApplicable(any())).thenCallRealMethod();

        Layer layer = Layer.builder().id(EventShapeLayerProvider.EVENT_SHAPE_LAYER_ID).build();

        when(eventApiProvider.obtainLayer(any(), any())).thenReturn(layer);

        List<Layer> result = layerService.get(Collections.emptyList(), List.of(layer.getId()),
                paramsWithSomeBoundary());
        verify(eventApiProvider, times(1)).obtainLayer(eq(layer.getId()),
                any());

        assertEquals(1, result.size());
    }

    @Test
    public void urbanLayersAreProcessedByUrbanProviderTest() {
        when(layersApiProvider.isApplicable(any())).thenCallRealMethod();
        when(eventApiProvider.isApplicable(any())).thenCallRealMethod();
        when(urbanAndPeripheryLayerProvider.isApplicable(any())).thenCallRealMethod();
        when(bivariateLayerProvider.isApplicable(any())).thenCallRealMethod();

        Layer layer = Layer.builder().id(URBAN_CORE_LAYER_ID).build();

        when(urbanAndPeripheryLayerProvider.obtainLayer(any(), any())).thenReturn(layer);

        List<Layer> result = layerService.get(Collections.emptyList(), List.of(layer.getId()),
                paramsWithSomeBoundary());
        verify(urbanAndPeripheryLayerProvider, times(1)).obtainLayer(eq(layer.getId()),
                any());

        assertEquals(1, result.size());
    }

    @Test
    public void bivariateLayersAreProcessedByBivariateProviderTest() {
        when(layersApiProvider.isApplicable(any())).thenCallRealMethod();
        when(eventApiProvider.isApplicable(any())).thenCallRealMethod();
        when(urbanAndPeripheryLayerProvider.isApplicable(any())).thenCallRealMethod();
        when(bivariateLayerProvider.isApplicable(any())).thenCallRealMethod();

        Layer layer = Layer.builder().id(BivariateLayerProvider.LAYER_PREFIX + "layerId").build();

        when(bivariateLayerProvider.obtainLayer(any(), any())).thenReturn(layer);

        List<Layer> result = layerService.get(Collections.emptyList(), List.of(layer.getId()),
                paramsWithSomeBoundary());
        verify(bivariateLayerProvider, times(1)).obtainLayer(eq(layer.getId()),
                any());

        assertEquals(1, result.size());
    }

    private void givenUserLayerIsReturnedOnlyWhenQueriedWithoutGeometry() {
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(eq(userLayer.getId()), argThat(p -> p.getBoundary() != null))).thenReturn(
                null);
        when(layersApiProvider.obtainLayer(eq(userLayer.getId()), argThat(p -> p.getBoundary() == null))).thenReturn(
                userLayer);
    }

    private void givenCommonLayerIsReturnedOnlyWhenQueriedWithGeometry() {
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(eq(commonLayer.getId()), argThat(p -> p.getBoundary() != null))).thenReturn(
                commonLayer);
        when(layersApiProvider.obtainLayer(eq(commonLayer.getId()), argThat(p -> p.getBoundary() == null))).thenReturn(
                null);
    }

    @Test
    public void globalOverlaysTest() {
        //all providers return nothing, so only global overlays should be returned
        List<Layer> layers = layerService.getGlobalLayers(paramsWithSomeAppId());
        //check all layers with 'globalOverlay: true' are present
        assertEquals(1, layers.size());
        assertEquals("activeContributors", layers.get(0).getId());
    }

    @Test
    public void getGlobalLayersShouldWorkEvenIfSomeProvidersThrowTest() {
        when(bivariateLayerProvider.obtainGlobalLayers(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "fail", HttpHeaders.EMPTY, null, Charset.defaultCharset()));
        when(layersApiProvider.obtainGlobalLayers(any())).thenReturn(
                CompletableFuture.completedFuture(List.of(layer)));

        List<Layer> result = layerService.getGlobalLayers(paramsWithSomeAppId());
        assertFalse(result.isEmpty());
        assertTrue(result.contains(layer));
    }

    @Test
    public void getUserLayersShouldWorkEvenIfSomeProvidersThrowTest() {
        when(eventApiProvider.obtainUserLayers(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "fail", HttpHeaders.EMPTY, null, Charset.defaultCharset()));
        when(layersApiProvider.obtainUserLayers(any())).thenReturn(
                CompletableFuture.completedFuture(List.of(layer)));

        List<Layer> result = layerService.getUserLayers(paramsWithSomeAppId());
        assertFalse(result.isEmpty());
        assertTrue(result.contains(layer));
    }

    @Test
    public void getSelectedAreaLayersShouldWorkEvenIfSomeProvidersThrowTest() {
        when(urbanAndPeripheryLayerProvider.obtainSelectedAreaLayers(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "fail", HttpHeaders.EMPTY, null, Charset.defaultCharset()));
        when(layersApiProvider.obtainSelectedAreaLayers(any())).thenReturn(
                CompletableFuture.completedFuture(List.of(layer)));

        List<Layer> result = layerService.getSelectedAreaLayers(paramsWithSomeBoundary());
        assertFalse(result.isEmpty());
        assertTrue(result.contains(layer));
    }

    @Test
    public void getShouldNotThrowIfAnyProviderThrowsTest() {
        when(urbanAndPeripheryLayerProvider.isApplicable(any())).thenReturn(true);
        when(urbanAndPeripheryLayerProvider.obtainLayer(any(), any())).thenThrow(HttpClientErrorException
                .create(HttpStatus.NOT_FOUND, "not found", HttpHeaders.EMPTY, null,
                        Charset.defaultCharset()));
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(any(), any())).thenReturn(userLayer);

        List<Layer> layers = layerService.get(List.of(SETTLED_PERIPHERY_LAYER_ID), List.of(userLayer.getId()),
                paramsWithSomeBoundary());
        assertFalse(layers.isEmpty());
    }

    @Test
    public void globalOverlaysGetTest() {
        //all providers return nothing (= no features matched by geometry), so global overlay should be returned
        List<Layer> layers = layerService.get(List.of("activeContributors"), List.of(), paramsWithSomeBoundary());
        assertEquals(1, layers.size());
        System.out.println(layers);
    }
}
