package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.dto.EventType.EARTHQUAKE;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.EVENT_SHAPE_LAYER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class EventShapeLayerProvTest extends LayerProvidersTest {

    @MockBean
    EventApiService eventApiService;

    @BeforeEach
    private void init() throws IOException {
        Mockito.when(eventApiService.getEvent(any(), any(), any())).thenReturn(
            objectMapper.readValue(
                getClass().getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
                EventDto.class));
    }

    @Test
    public void list_emptyEventId() {
        assertNull(eventShapeLayerProvider.obtainLayers(null, null));
    }

    @Test
    public void get_emptyEventId() {
        assertThrows(WebApplicationException.class, () -> {
            assertNull(eventShapeLayerProvider.obtainLayer(new Point(new double[]{0d, 0d}),
                EVENT_SHAPE_LAYER_ID, null)
            );
        });
    }

    @Test
    public void list_Earthquake() {
        List<Layer> results = eventShapeLayerProvider.obtainLayers(null, UUID.randomUUID());
        assertEquals(1, results.size());
        Layer result = results.get(0);

        //there are "Class" properties in features - which define further layer id hence layer config
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID + "." + EARTHQUAKE, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void get_Earthquake() {
        Layer result = eventShapeLayerProvider.obtainLayer(null, EVENT_SHAPE_LAYER_ID, UUID.randomUUID());

        //there are "Class" properties in features - which define further layer id hence layer config
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID + "." + EARTHQUAKE, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void get_Default() throws IOException {
        EventDto eventDto = objectMapper.readValue(getClass()
                .getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
            EventDto.class);
        //remove "Class" entries from features properties
        Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures()).forEach(feature -> {
            if (feature.getProperties() != null) {
                feature.getProperties().remove("Class");
            }
        });
        Mockito.when(eventApiService.getEvent(any(), any(), any())).thenReturn(eventDto);

        Layer result = eventShapeLayerProvider.obtainLayer(null, EVENT_SHAPE_LAYER_ID, UUID.randomUUID());
        //"Class" property is absent from features properties => the default template is used
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID, result.getId());
        //check source data was loaded
        Assertions.assertEquals(2, result.getSource().getData().getFeatures().length);
        Assertions.assertEquals("Point", result.getSource().getData().getFeatures()[0].getGeometry().getType());
        Assertions.assertEquals("Polygon", result.getSource().getData().getFeatures()[1].getGeometry().getType());
    }

    @Test
    public void list_Default() throws IOException {
        EventDto eventDto = objectMapper.readValue(getClass()
                .getResource("/io/kontur/disasterninja/client/layers/eventdto.json"),
            EventDto.class);

        //remove "Class" entries from features properties
        Arrays.stream(eventDto.getLatestEpisodeGeojson().getFeatures()).forEach(feature -> {
            if (feature.getProperties() != null) {
                feature.getProperties().remove("Class");
            }
        });
        Mockito.when(eventApiService.getEvent(any(), any(), any())).thenReturn(eventDto);

        List<Layer> result = eventShapeLayerProvider.obtainLayers(null, UUID.randomUUID());
        assertEquals(1, result.size());
        //"Class" property is absent from features properties => the default template is used
        Assertions.assertEquals(EVENT_SHAPE_LAYER_ID, result.get(0).getId());
    }

    @Test
    public void list_NoIntersection() {
        assertNull(eventShapeLayerProvider.obtainLayers(new Point(new double[]{0d, 0d}), UUID.randomUUID()));
    }

    @Test
    public void get_NoIntersection() {
        assertNull(eventShapeLayerProvider.obtainLayer(new Point(new double[]{0d, 0d}), EVENT_SHAPE_LAYER_ID,
            UUID.randomUUID()));
    }
}
