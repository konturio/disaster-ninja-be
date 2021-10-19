package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import k2layers.api.model.GeometryGeoJSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.FeatureCollection;

import java.util.List;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InsightsApiClientIT {

    @Autowired
    InsightsApiClient insightsApiClient;

    @Test
    public void insightsApiTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";
        FeatureCollection dto = insightsApiClient.getUrbanCoreAndSettledPeripheryLayers(new ObjectMapper().readValue(json,
            GeometryGeoJSON.class));

        List<Layer> layers = Layer.fromUrbanCodeAndPeripheryLayer(dto);
        Assertions.assertFalse(layers.isEmpty());
    }
}
