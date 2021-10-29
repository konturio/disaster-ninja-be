package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Getter
@ConfigurationProperties(prefix = "layers")
public class LocalLayersConfig {
    private final List<Layer> configs;
}
