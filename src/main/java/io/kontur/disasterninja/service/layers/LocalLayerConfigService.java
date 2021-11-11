package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.LegendStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocalLayerConfigService implements LayerConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLayerConfigService.class);
    private final Map<String, Layer> globalOverlays = new HashMap<>();
    private final Map<String, Layer> regularLayers = new HashMap<>();

    public LocalLayerConfigService(LocalLayersConfig localLayersConfig,
                                   @Value("${kontur.platform.tiles.host}") String tilesHost) {
        try {
            localLayersConfig.getConfigs().forEach(this::setLegendStepsOrder);

            localLayersConfig.getConfigs().forEach((config) -> {
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
            });

            LOG.info("Loaded {} regular layer configurations: {}", regularLayers.values().size(), regularLayers.keySet());
            LOG.info("Loaded {} global overlay layers: {}", globalOverlays.values().size(), globalOverlays.keySet());
        } catch (Exception e) {
            LOG.error("Cannot load layer configurations! {}", e.getMessage(), e);
        }
    }

    @Override
    public void applyConfig(Layer input) {
        Layer config = regularLayers.get(input.getId());
        if (config == null) {
            config = globalOverlays.get(input.getId());
        }

        if (config != null) {
            input.mergeFrom(config);
        }
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
}
