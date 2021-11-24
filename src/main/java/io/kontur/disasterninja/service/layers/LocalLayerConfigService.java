package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

@Service
public class LocalLayerConfigService implements LayerConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLayerConfigService.class);
    private final Map<String, Layer> globalOverlays = new HashMap<>();
    private final Map<String, Layer> regularLayers = new HashMap<>();
    private final MessageSource messageSource;

    public LocalLayerConfigService(LocalLayersConfig localLayersConfig,
                                   @Value("${kontur.platform.tiles.host}") String tilesHost,
                                   MessageSource messageSource) {
        this.messageSource = messageSource;
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

                //fixed list of fields to localize
                localizeField(ENGLISH, config::getName, config::setName);
                localizeField(ENGLISH, config::getDescription, config::setDescription);
                localizeField(ENGLISH, () -> config.getCategory() != null ? config.getCategory().toString() : null,
                    v -> config.setCategory(LayerCategory.fromString(v)));
                localizeField(ENGLISH, config::getGroup, config::setGroup);
                if (config.getLegend() != null && config.getLegend().getSteps() != null) {
                    config.getLegend().getSteps().forEach(it -> localizeField(ENGLISH, it::getStepName, it::setStepName));
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

    private void localizeField(Locale locale, Supplier<String> getter, Consumer<String> setter) {
        String value = getter.get();
        if (toBeReplaced(value)) {
            StringBuilder name = new StringBuilder(value);
            Matcher matcher = Pattern.compile("#\\{[^{]+}").matcher(name);

            while (matcher.find()) {
                MatchResult matchResult = matcher.toMatchResult();
                String key = matcher.group().replaceAll("#\\{", "").replaceAll("}", "");

                name.replace(matchResult.start(), matchResult.end(), messageSource.getMessage(key, null, locale));
                matcher = Pattern.compile("#\\{[^{]+}").matcher(name);
            }
            setter.accept(name.toString());
        }
    }

    private boolean toBeReplaced(String value) {
        return value != null && Pattern.compile("(.?)+#\\{[^{]+}(.?)+").matcher(value).matches();
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
