package io.kontur.disasterninja.service.layers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.LayersApiApplicationDto;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.util.TestUtil.createLegend;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LayersApiServiceTest {

    final String id = "myId";
    final String title = "layer title";
    final Legend legend = createLegend();

    @Mock
    private LayersApiClient layersApiClient;
    @InjectMocks
    private LayersApiService layersApiService;

    @Test
    public void findLayersTest() throws JsonProcessingException {
        //given
        when(layersApiClient.getCollections(any(), anyBoolean(), any(), any())).thenReturn(List.of(collection()));

        //when
        List<Layer> layers =
                layersApiService.findLayers(null, false, null, null);

        //then
        verify(layersApiClient, times(1))
                .getCollections(null, false, null, null);
        assertEquals(1, layers.size());
        assertLayer(layers.get(0));
    }

    @Test
    public void getLayerTest() throws JsonProcessingException {
        //given
        when(layersApiClient.getCollection(any(), any())).thenReturn(collection());

        //when
        Layer layer = layersApiService.getLayer(null, id, null);

        //then
        verify(layersApiClient, times(1)).getCollection(id, null);
        assertLayer(layer);
    }

    @Test
    public void createLayerTest() throws JsonProcessingException {
        //given
        LayerCreateDto layerCreateDto = layerCreateDto();
        when(layersApiClient.createCollection(any())).thenReturn(collection());

        //when
        Layer layer = layersApiService.createLayer(layerCreateDto);

        //then
        verify(layersApiClient, times(1)).createCollection(layerCreateDto);
        assertLayer(layer);
    }

    @Test
    public void createLayerNoPermissionsTest() {
        givenLayersApiClientCreateCollectionRespondsWith403();
        LayerCreateDto dto = layerCreateDto();

        assertThrows(HttpClientErrorException.Forbidden.class, () -> layersApiService.createLayer(dto));
    }

    @Test
    public void updateLayerTest() throws JsonProcessingException {
        //given
        LayerUpdateDto layerUpdateDto = layerCreateDto();
        when(layersApiClient.updateCollection(any(), any())).thenReturn(collection());

        //when
        Layer layer = layersApiService.updateLayer(id, layerUpdateDto);

        //then
        verify(layersApiClient, times(1)).updateCollection(id, layerUpdateDto);
        assertLayer(layer);
    }

    @Test
    public void deleteLayerTest() {
        //given
        doNothing().when(layersApiClient).deleteCollection(any());

        //when
        layersApiService.deleteLayer(id);

        //then
        verify(layersApiClient, times(1)).deleteCollection(id);
    }

    @Test
    public void getApplicationLayersTest() throws JsonProcessingException {
        //given
        UUID appId = UUID.randomUUID();
        when(layersApiClient.getApplicationLayers(any())).thenReturn(layersApiApplicationDto());

        //when
        List<Layer> layers = layersApiService.getApplicationLayers(appId);

        //then
        verify(layersApiClient, times(1)).getApplicationLayers(appId);
        assertEquals(1, layers.size());
        assertLayer(layers.get(0));
    }

    @Test
    public void getApplicationLayersEmptyTest() {
        //given
        UUID appId = UUID.randomUUID();
        LayersApiApplicationDto layersApiApplicationDto =
                new LayersApiApplicationDto(UUID.randomUUID(), false, true, emptyList());
        when(layersApiClient.getApplicationLayers(any())).thenReturn(layersApiApplicationDto);

        //when
        List<Layer> layers = layersApiService.getApplicationLayers(appId);

        //then
        verify(layersApiClient, times(1)).getApplicationLayers(appId);
        assertTrue(layers.isEmpty());
    }

    @Test
    public void updateApplicationLayersTest() throws JsonProcessingException {
        //given
        UUID appId = UUID.randomUUID();
        AppLayerUpdateDto appLayerUpdateDto = new AppLayerUpdateDto(id, false, null);
        when(layersApiClient.updateApplicationLayers(any(), any())).thenReturn(layersApiApplicationDto());

        //when
        List<Layer> layers = layersApiService.updateApplicationLayers(appId, List.of(appLayerUpdateDto));

        //then
        verify(layersApiClient, times(1))
                .updateApplicationLayers(appId, List.of(appLayerUpdateDto));
        assertEquals(1, layers.size());
        assertLayer(layers.get(0));
    }

    private void givenLayersApiClientCreateCollectionRespondsWith403() {
        when(layersApiClient.createCollection(any()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "forbidden",
                        new HttpHeaders(), null, Charset.defaultCharset()));
    }

    private LayerCreateDto layerCreateDto() {
        LayerCreateDto dto = new LayerCreateDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setLegend(legend);
        return dto;
    }

    private LayersApiApplicationDto layersApiApplicationDto() throws JsonProcessingException {
        return new LayersApiApplicationDto(UUID.randomUUID(), false, true,
                List.of(collection()));
    }

    private Collection collection() throws JsonProcessingException {
        Collection collection = new Collection();
        collection.setId(id);
        collection.setTitle(title);
        collection.setStyleRule(new ObjectMapper().readValue("{\"name\":\"legendName\",\"type\":\"simple\"," +
                "\"linkProperty\":null,\"steps\":[{\"paramName\":\"param name\",\"paramValue\":\"qwe\",\"axis\":null," +
                "\"axisValue\":null,\"stepName\":\"step name\",\"stepShape\":\"hex\",\"style\":{\"prop\":\"value\"}," +
                "\"sourceLayer\":\"source-layer\",\"stepIconFill\":\"fill\",\"stepIconStroke\":\"stroke\"}," +
                "{\"paramName\":\"param name\",\"paramValue\":\"asd\",\"axis\":null,\"axisValue\":null," +
                "\"stepName\":\"step name2\",\"stepShape\":\"hex\",\"style\":{\"prop\":\"value\"}," +
                "\"sourceLayer\":\"source-layer\",\"stepIconFill\":\"\",\"stepIconStroke\":\"\"}]," +
                "\"tooltip\": {\"type\": \"markdown\",\"paramName\": \"tooltipContent\"},\"colors\": " +
                "[{\"id\":\"A1\",\"color\":\"rgb(232,232,157)\"}],\"axes\":{\"x\":{\"label\": \"xLabel\"}," +
                "\"y\":{\"label\": \"yLabel\"}}}", ObjectNode.class));

        return collection;
    }

    private void assertLayer(Layer layer) {
        assertEquals(id, layer.getId());
        assertEquals(title, layer.getName());
        assertEquals(legend, layer.getLegend());
    }
}
