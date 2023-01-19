package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LayerProvider {

    @Async
    CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams);

    @Async
    CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams);

    @Async
    CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams);

    Layer obtainLayer(String layerId, LayerSearchParams searchParams);

    boolean isApplicable(String layerId);
}
