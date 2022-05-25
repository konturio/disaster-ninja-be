package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.service.layers.LocalLayerConfigService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Point;

import java.util.UUID;

import static io.kontur.disasterninja.service.layers.providers.EventShapeLayerProvider.EVENT_SHAPE_LAYER_ID;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventShapeLayerProviderIT {

    @Autowired
    EventShapeLayerProvider provider;
    @Autowired
    LocalLayerConfigService layerConfigService;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void test() throws JsonProcessingException {
        Layer l = provider.obtainLayer(EVENT_SHAPE_LAYER_ID, LayerSearchParams.builder()
                .eventFeed("kontur-public")
                .eventId(UUID.fromString("29a8c8f6-0121-4a3e-8065-41ce0028f8d2")) //sonic
//                .eventId(UUID.fromString("905e27af-15d7-4c40-9b08-03436855ea51")) //zigzag
                .boundary(new Point(new double[]{152.5386, -5.3964}))
                .build());
        layerConfigService.applyConfig(l);

        System.out.println(objectMapper.writeValueAsString(LayerSummaryDto.fromLayer(l)));
        System.out.println(objectMapper.writeValueAsString(LayerDetailsDto.fromLayer(l)));
    }
}
