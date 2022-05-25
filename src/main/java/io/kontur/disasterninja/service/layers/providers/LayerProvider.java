package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LayerProvider {

    /**
     * Layer.Source is not populated for layers returned!
     *
     * @return list of layers available from this LayerProvider.
     */
    @Async
    CompletableFuture<List<Layer>> obtainLayers(LayerSearchParams searchParams);

    Layer obtainLayer(String layerId, LayerSearchParams searchParams);

    boolean isApplicable(String layerId);
}
