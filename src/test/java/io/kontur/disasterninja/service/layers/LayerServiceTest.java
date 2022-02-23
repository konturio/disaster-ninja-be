package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layer.LegendDto;
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
import java.util.List;

import static io.kontur.disasterninja.client.LayersApiClient.LAYER_PREFIX;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.HOT_LAYER_ID;
import static io.kontur.disasterninja.util.TestUtil.createLegend;
import static io.kontur.disasterninja.util.TestUtil.paramsWithSomeBoundary;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LayerServiceTest {

    final String id = "myId";
    final String title = "layer title";
    final Legend legend = createLegend();
    final LegendDto legendDto = LegendDto.fromLegend(legend);
    final Layer layer = Layer.builder()
        .id(id)
        .name(title)
        .legend(legend)
        .build();

    final Layer userLayer = Layer.builder().id(LAYER_PREFIX + "userLayer").build();
    final Layer commonLayer = Layer.builder().id(LAYER_PREFIX + "commonLayer").build();

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
    @MockBean
    LayersApiClient layersApiClient;
    @MockBean
    LayersApiProvider layersApiProvider;

    @Autowired
    LayerService layerService;

    @BeforeEach
    public void beforeEach() {
        when(hotLayerProvider.obtainLayers(any())).thenReturn(new ArrayList<>());
        when(osmLayerProvider.obtainLayers(any())).thenReturn(new ArrayList<>());
        when(urbanAndPeripheryLayerProvider.obtainLayers(any())).thenReturn(new ArrayList<>());
        when(eventApiProvider.obtainLayers(any())).thenReturn(new ArrayList<>());
        when(bivariateLayerProvider.obtainLayers(any())).thenReturn(new ArrayList<>());
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

    private void givenUserLayerIsReturnedOnlyWhenQueriedWithoutGeometry() {
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(eq(userLayer.getId()), argThat(p -> p.getBoundary() != null))).thenReturn(null);
        when(layersApiProvider.obtainLayer(eq(userLayer.getId()), argThat(p -> p.getBoundary() == null))).thenReturn(userLayer);
    }

    private void givenCommonLayerIsReturnedOnlyWhenQueriedWithGeometry() {
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(eq(commonLayer.getId()), argThat(p -> p.getBoundary() != null))).thenReturn(commonLayer);
        when(layersApiProvider.obtainLayer(eq(commonLayer.getId()), argThat(p -> p.getBoundary() == null))).thenReturn(null);
    }

    @Test
    public void globalOverlaysListTest() {
        //all providers return nothing (= no features matched by geometry), so only global overlays should be returned
        List<Layer> layers = layerService.getList(paramsWithSomeBoundary());
        //check all layers with 'globalOverlay: true' are present
        assertEquals(9, layers.size());
        assertEquals("Kontur OpenStreetMap Quantity", layers.get(0).getId());
        assertEquals("Kontur OpenStreetMap Building Quantity", layers.get(1).getId());
        assertEquals("Kontur OpenStreetMap Road Length", layers.get(2).getId());
        assertEquals("Kontur OpenStreetMap Mapping Activity", layers.get(3).getId());
        assertEquals("Kontur OpenStreetMap Antiquity", layers.get(4).getId());
        assertEquals("Kontur Nighttime Heatwave Risk", layers.get(5).getId());
        assertEquals("Kontur Fire Service Scarcity Risk", layers.get(6).getId());

        assertEquals("activeContributors", layers.get(7).getId());
        assertEquals("hotProjectsTileLayer", layers.get(8).getId());

        System.out.println(layers);
    }

    @Test
    public void getListShouldWorkEvenIfSomeProvidersThrowTest() {
        when(hotLayerProvider.obtainLayers(any())).thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST,
            "fail", null,null, Charset.defaultCharset()));
        when(layersApiProvider.obtainLayers(any())).thenReturn(List.of(layer));

        List<Layer> result = layerService.getList(paramsWithSomeBoundary());
        assertFalse(result.isEmpty());
        assertTrue(result.contains(layer));
    }

    @Test
    public void getShouldThrowIfAnyProviderThrowsTest() {
        when(hotLayerProvider.isApplicable(any())).thenReturn(true);
        when(hotLayerProvider.obtainLayer(any(), any())).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND,
            "not found", null,null, Charset.defaultCharset()));
        when(layersApiProvider.isApplicable(any())).thenReturn(true);
        when(layersApiProvider.obtainLayer(any(), any())).thenReturn(userLayer);

        try {
            layerService.get(List.of(HOT_LAYER_ID), List.of(userLayer.getId()), paramsWithSomeBoundary());
            throw new RuntimeException("expected exception was not thrown!");
        } catch (HttpClientErrorException.NotFound e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }

    @Test
    public void globalOverlaysGetTest() {
        //all providers return nothing (= no features matched by geometry), so global overlay should be returned
        List<Layer> layers = layerService.get(List.of("Kontur Nighttime Heatwave Risk"),
            List.of(), paramsWithSomeBoundary());
        assertEquals(1, layers.size());
        System.out.println(layers);
    }

    @Test
    public void createTest() {
        when(layersApiClient.createLayer(any())).thenReturn(layer);

        LayerCreateDto dto = createDto();
        Layer layer = layerService.create(dto);

        verify(layersApiClient, times(1)).createLayer(dto);
        assertLayer(layer);
    }

    @Test
    public void createNoPermissionsTest() {
        givenLayerApiCreateRespondsWith403();

        LayerCreateDto dto = createDto();
        assertThrows(HttpClientErrorException.Forbidden.class, () -> layerService.create(dto));
    }

    @Test
    public void updateTest() {
        when(layersApiClient.updateLayer(matches(id), any())).thenReturn(layer);

        LayerUpdateDto dto = updateDto();
        Layer layer = layerService.update(id, dto);

        verify(layersApiClient, times(1)).updateLayer(id, dto);
        assertLayer(layer);
    }

    @Test
    public void updateNoPermissionsTest() {
        givenLayerApiUpdateRespondsWith403();

        LayerUpdateDto dto = updateDto();
        assertThrows(HttpClientErrorException.Forbidden.class,
            () -> layerService.update("123", dto));
    }

    @Test
    public void deleteTest() {
        layerService.delete(id);

        verify(layersApiClient, times(1)).deleteLayer(id);
    }

    @Test
    public void deleteNoPermissionsTest() {
        givenLayerApiDeleteRespondsWith401();

        assertThrows(HttpClientErrorException.Unauthorized.class,
            () -> layerService.delete("123"));
    }

    private LayerUpdateDto updateDto() {
        LayerUpdateDto dto = new LayerUpdateDto();
        dto.setTitle(title);
        dto.setLegend(legendDto);
        return dto;
    }

    private LayerCreateDto createDto() {
        LayerCreateDto dto = new LayerCreateDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setLegend(legendDto);
        return dto;
    }

    private void givenLayerApiCreateRespondsWith403() {
        when(layersApiClient.createLayer(any()))
            .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "forbidden",
                new HttpHeaders(), null, Charset.defaultCharset()));
    }

    private void givenLayerApiUpdateRespondsWith403() {
        when(layersApiClient.updateLayer(any(), any()))
            .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "forbidden",
                new HttpHeaders(), null, Charset.defaultCharset()));
    }

    private void givenLayerApiDeleteRespondsWith401() {
        doThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "unauthorized",
            new HttpHeaders(), null, Charset.defaultCharset()))
            .when(layersApiClient).deleteLayer(any());
    }

    private void assertLayer(Layer layer) {
        assertEquals(id, layer.getId());
        assertEquals(title, layer.getName());
        assertEquals(legend, layer.getLegend());
    }
}
