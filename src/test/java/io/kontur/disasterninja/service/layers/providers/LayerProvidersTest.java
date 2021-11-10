package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LayerProvidersTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    OsmLayerProvider osmLayerProvider;

    @Autowired
    HotLayerProvider hotLayerProvider;

    @Autowired
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;

    @Autowired
    EventShapeLayerProvider eventShapeLayerProvider;

    @Autowired
    BivariateLayerProvider bivariateLayerProvider;

    @BeforeEach
    private void setup() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
