package io.kontur.disasterninja.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.kontur.disasterninja.domain.Layer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LayerPrototypeServiceTest {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Test
    public void test() throws IOException {
        objectMapper.findAndRegisterModules();

        List<Layer> list = objectMapper.readValue(new File("src/test/resources/io/kontur/disasterninja/" +
            "service/layerconfig.yaml"), new TypeReference<>() {
        });

    }
}
