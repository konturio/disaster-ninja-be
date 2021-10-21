package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.service.layers.LayerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InsightsApiClientIT {

    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    LayerFactory layerFactory;

    @Test
    public void insightsApiTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";
        FeatureCollection dto = insightsApiClient.getUrbanCoreAndSettledPeripheryLayers(new ObjectMapper().readValue(json,
            Geometry.class));

        List<Layer> layers = layerFactory.fromUrbanCodeAndPeripheryLayer(dto);
        Assertions.assertFalse(layers.isEmpty());
    }
}
