package io.kontur.disasterninja.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class LayerConfigServiceTest {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    public void test() throws IOException {
        objectMapper.findAndRegisterModules();

        List<Layer> list = objectMapper.readValue(
            ClassLoader.getSystemResource("layers/layerconfig.yaml"), new TypeReference<>() {
        });
        //todo assert?
    }
}
