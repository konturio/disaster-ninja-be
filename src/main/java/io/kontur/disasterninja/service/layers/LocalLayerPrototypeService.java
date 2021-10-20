package io.kontur.disasterninja.service.layers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.kontur.disasterninja.domain.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocalLayerPrototypeService implements LayerPrototypeService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLayerPrototypeService.class);
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    Map<String, Layer> defaults;

    public LocalLayerPrototypeService() {
        objectMapper.findAndRegisterModules();
        try {
            List<Layer> list = objectMapper.readValue(new File("src/main/resources/layerconfig.yaml"),
                new TypeReference<>() {
                });
            defaults = list.stream().collect(Collectors.toMap(Layer::getId, l -> l));
        } catch (IOException e) {
            LOG.error("Cannot load layer configurations! {}", e.getMessage(), e);
            defaults = new HashMap<>();
        }
    }

    @Override
    public Layer prototypeOrEmpty(String layerId) {
        Layer prototype = defaults.get(layerId);
        if (prototype == null) {
            return new Layer(layerId);
        }
        return objectMapper.convertValue(prototype, Layer.class); //clone
    }
}
