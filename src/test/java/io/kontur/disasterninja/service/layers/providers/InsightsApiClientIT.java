package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import static io.kontur.disasterninja.service.layers.providers.LayerProvider.SETTL_PERIPHERY_LAYER_ID;
import static io.kontur.disasterninja.service.layers.providers.LayerProvider.URBAN_CORE_LAYER_ID;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InsightsApiClientIT {

    @Autowired
    InsightsApiClient insightsApiClient;
    @Autowired
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;

    @Test
    public void insightsApiTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";
        FeatureCollection dto = insightsApiClient.getUrbanCoreAndSettledPeripheryLayers(new ObjectMapper().readValue(json,
            Geometry.class));

        Layer urban = urbanAndPeripheryLayerProvider.urbanOrPeripheryLayer(dto, URBAN_CORE_LAYER_ID, false);
        Assertions.assertNotNull(urban);
        Layer periphery = urbanAndPeripheryLayerProvider.urbanOrPeripheryLayer(dto, SETTL_PERIPHERY_LAYER_ID, false);
        Assertions.assertNotNull(periphery);
    }
}
