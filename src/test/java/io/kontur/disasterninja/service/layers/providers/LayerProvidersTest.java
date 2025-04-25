package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LayerProvidersTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UrbanAndPeripheryLayerProvider urbanAndPeripheryLayerProvider;

    @Autowired
    EventShapeLayerProvider eventShapeLayerProvider;

}
