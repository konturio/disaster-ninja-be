package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.List;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KcApiClientIT {

    @Autowired
    KcApiClient kcApiClient;

    @Autowired
    HotLayerProvider hotLayerProvider;

    @Autowired
    OsmLayerProvider osmLayerProvider;

    @Test
    public void kcOsmTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";
        List<Feature> features = kcApiClient.getOsmLayers(new ObjectMapper().readValue(json,
            Geometry.class));
        List<Layer> layers = osmLayerProvider.fromOsmLayers(features);
        Assertions.assertFalse(layers.isEmpty());
    }

    @Test
    public void kcHotTest() throws JsonProcessingException {
        String json = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-10.654764175,6.276395795],[-10.707979202," +
            "6.276395795],[-10.7264328,6.274774784],[-10.73141098,6.260612054],[-10.696048737,6.226739391]," +
            "[-10.656738281,6.219060146],[-10.651073456,6.246363619],[-10.649528503,6.265304567]," +
            "[-10.654764175,6.276395795]]]]}";
        List<Feature> features = kcApiClient.getHotProjectLayer(new ObjectMapper().readValue(json,
            Geometry.class));
        Layer layer = hotLayerProvider.fromHotProjectLayers(features);
        Assertions.assertNotNull(layer);
    }
}
