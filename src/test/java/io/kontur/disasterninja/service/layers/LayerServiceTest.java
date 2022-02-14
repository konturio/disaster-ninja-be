package io.kontur.disasterninja.service.layers;

import static io.kontur.disasterninja.service.LayersApiService.LAYER_PREFIX;
import static io.kontur.disasterninja.util.TestUtil.createLegend;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layer.LegendDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import io.kontur.disasterninja.service.layers.providers.BivariateLayerProvider;
import io.kontur.disasterninja.service.layers.providers.EventShapeLayerProvider;
import io.kontur.disasterninja.service.layers.providers.HotLayerProvider;
import io.kontur.disasterninja.service.layers.providers.OsmLayerProvider;
import io.kontur.disasterninja.service.layers.providers.UrbanAndPeripheryLayerProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.Point;

@SpringBootTest
public class LayerServiceTest {

    final String id = "myId";
    final String title = "layer title";
    final Legend legend = createLegend();
    final LegendDto legendDto = LegendDto.fromLegend(legend);

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
        assertEquals(8, layers.size());
        assertEquals("Kontur OpenStreetMap Quantity", layers.get(0).getId());
        assertEquals("Kontur OpenStreetMap Building Quantity", layers.get(1).getId());
        assertEquals("Kontur OpenStreetMap Road Length", layers.get(2).getId());
        assertEquals("Kontur OpenStreetMap Mapping Activity", layers.get(3).getId());
        assertEquals("Kontur OpenStreetMap Antiquity", layers.get(4).getId());
        assertEquals("Kontur Nighttime Heatwave Risk", layers.get(5).getId());
        System.out.println(layers);
    }

    @Test
    public void globalOverlaysGetTest() {
        //all providers return nothing (= no features matched by geometry), so global overlay should be returned
        List<Layer> layers = layerService.get(new Point(new double[]{1, 2}), List.of("Kontur Nighttime Heatwave Risk"),
            null);
        assertEquals(1, layers.size());
        System.out.println(layers);
    }

    @Test
    public void createTest() {
        LayerCreateDto dto = createDto();
        givenLayerApiCreateRespondsWithLayer();

        Layer layer = layerService.create(dto);

        verify(layersApiClient, times(1)).createCollection(dto);
        assertLayer(layer);
    }

    @Test
    public void updateTest() {
        LayerUpdateDto dto = updateDto();
        givenLayerApiUpdateRespondsWithLayer();

        Layer layer = layerService.update(id, dto);

        verify(layersApiClient, times(1)).updateCollection(id, dto);
        assertLayer(layer);
    }

    @Test
    public void deleteTest() {
        layerService.delete(id);

        verify(layersApiClient, times(1)).deleteCollection(id);
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

    private void givenLayerApiCreateRespondsWithLayer() {
        Collection layerApiResponse = new Collection(id, title, null, null,
            null, legendDto, null, null, null, null);

        when(layersApiClient.createCollection(any())).thenReturn(layerApiResponse);
    }

    private void givenLayerApiUpdateRespondsWithLayer() {
        Collection layerApiResponse = new Collection(id, title, null, null,
            null, legendDto, null, null, null, null);

        when(layersApiClient.updateCollection(matches(id), any())).thenReturn(layerApiResponse);
    }

    private void assertLayer(Layer layer) {
        assertEquals(LAYER_PREFIX + id, layer.getId());
        assertEquals(title, layer.getName());
        assertEquals(legend, layer.getLegend());
    }
}
