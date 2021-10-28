package io.kontur.disasterninja.service.layers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LegendStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocalLayerConfigService implements LayerConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLayerConfigService.class);
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    Map<String, Layer> defaults;

    public LocalLayerConfigService() {
        objectMapper.findAndRegisterModules();
        try {
            List<Layer> layers = objectMapper.readValue(getClass().getResourceAsStream("/layers/layerconfig.yaml"),
                new TypeReference<>() {
                });
            layers.forEach(this::setLegendStepsOrder);
            defaults = layers.stream().collect(Collectors.toMap(Layer::getId, l -> l));
        } catch (IOException e) {
            LOG.error("Cannot load layer configurations! {}", e.getMessage(), e);
            defaults = new HashMap<>();
        }
    }

    @Override
    public void applyConfigs(Map<String, Layer> layers) {
        //apply layer configs
        defaults.forEach((layerName, config) -> {
            if (!layers.containsKey(layerName)) {
                //only global overlays are added if not received from providers
                if (config.isGlobalOverlay()) {
                    layers.put(layerName, config);
                }
            } else {
                //apply configs to loaded layers
                layers.get(layerName).mergeFrom(config);
            }
        });
    }

    @Override
    public void applyConfig(Layer input) {
        Layer config = defaults.get(input.getId());
        if (config != null) {
            input.mergeFrom(config);
        }
    }

    @Override
    public Map<String, Layer> getConfigs() {
        return defaults;
    }

    private void setLegendStepsOrder(Layer layer) {
        if (layer.getLegend() != null && layer.getLegend().getSteps() != null) {
            List<LegendStep> steps = layer.getLegend().getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setOrder(i);
            }
        }
    }
}
