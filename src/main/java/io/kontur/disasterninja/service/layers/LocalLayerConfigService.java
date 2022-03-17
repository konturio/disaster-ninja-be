package io.kontur.disasterninja.service.layers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.LegendStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocalLayerConfigService implements LayerConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLayerConfigService.class);
    private final Map<String, Layer> globalOverlays = new HashMap<>();
    private final Map<String, Layer> regularLayers = new HashMap<>();

    public LocalLayerConfigService(@Value("classpath:/layers/layerconfig.yaml") Resource layersJsonResource,
                                   @Value("${kontur.platform.tiles.host}") String tilesHost,
                                   @Value("${spring.profiles.active:dev}") String profile) {
        try {
            LocalLayersConfig localLayersConfig = convertLayerConfig(layersJsonResource);
            localLayersConfig.getConfigs().forEach(this::setLegendStepsOrder);

            localLayersConfig.getConfigs().forEach((config) -> {
                if (!config.isTestOnly() || !"prod".equalsIgnoreCase(profile)) {
                    LayerSource source = config.getSource();
                    if (source != null && source.getUrls() != null) {
                        source.setUrls(source.getUrls().stream()
                            .map(it -> {
                                //todo spring messages?
                                if (it.contains("{tilesHost}")) {
                                    return it.replaceAll("\\{tilesHost}", tilesHost);
                                } else {
                                    return it;
                                }
                            }).collect(Collectors.toList()));
                    }
                    if (config.isGlobalOverlay()) {
                        globalOverlays.put(config.getId(), config);
                    } else {
                        regularLayers.put(config.getId(), config);
                    }
                }
            });

            LOG.info("Loaded {} regular layer configurations: {}", regularLayers.values().size(), regularLayers.keySet());
            LOG.info("Loaded {} global overlay layers: {}", globalOverlays.values().size(), globalOverlays.keySet());
        } catch (Exception e) {
            LOG.error("Cannot load layer configurations! {}", e.getMessage(), e);
        }
    }

    @Override
    public void applyConfig(Layer input) {
        Layer config = getConfigForRegularLayer(input);

        if (config == null) {
            config = globalOverlays.get(input.getId());
        }

        if (config != null) {
            input.mergeFrom(config);
        }
    }

    private Layer getConfigForRegularLayer(Layer input) {
        if (input.getEventType() != null) {
            return getConfigForEventShapeLayer(input);
        } else {
            return regularLayers.get(input.getId());
        }
    }

    private Layer getConfigForEventShapeLayer(Layer input) {
        String eventShapeWithTypeConfigId = input.getId() + "." + input.getEventType().toString();
        Layer eventShapeWithTypeConfig = (regularLayers.get(eventShapeWithTypeConfigId));
        if (eventShapeWithTypeConfig != null) {
            return eventShapeWithTypeConfig;
        }
        return regularLayers.get(input.getId());
    }

    private String getConfigLayerId(Layer layer) {
        if (layer.getEventType() != null) {
            return layer.getId() + "." + layer.getEventType().toString();
        }
        return layer.getId();
    }

    @Override
    public Map<String, Layer> getGlobalOverlays() {
        return globalOverlays;
    }

    private void setLegendStepsOrder(Layer layer) {
        if (layer.getLegend() != null && layer.getLegend().getSteps() != null) {
            List<LegendStep> steps = layer.getLegend().getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setOrder(i);
            }
        }
    }

    private LocalLayersConfig convertLayerConfig(Resource layersJson) throws IOException {
        String json = new String(layersJson.getInputStream().readAllBytes());
        ObjectMapper mapper = YAMLMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.readValue(json, LocalLayersConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
