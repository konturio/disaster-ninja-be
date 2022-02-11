package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.Layer;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Jacksonized
@Builder
public class LocalLayersConfig {

    private final List<Layer> configs;

}
