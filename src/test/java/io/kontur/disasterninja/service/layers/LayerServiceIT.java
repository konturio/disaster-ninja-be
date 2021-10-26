package io.kontur.disasterninja.service.layers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wololo.geojson.Geometry;

import java.util.List;

@Disabled("just for local debugging")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LayerServiceIT {
    @Autowired
    LayerService layerService;

    @Test
    public void test() throws JsonProcessingException {
        String json = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-10.654764175,6.276395795],[-10.707979202," +
            "6.276395795],[-10.7264328,6.274774784],[-10.73141098,6.260612054],[-10.696048737,6.226739391]," +
            "[-10.656738281,6.219060146],[-10.651073456,6.246363619],[-10.649528503,6.265304567]," +
            "[-10.654764175,6.276395795]]]]}";
        List<Layer> layers = layerService.getList(new ObjectMapper().readValue(json,
            Geometry.class), null);
        Assertions.assertFalse(layers.isEmpty());
    }
}
