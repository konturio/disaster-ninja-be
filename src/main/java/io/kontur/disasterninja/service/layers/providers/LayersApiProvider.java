package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.util.AuthenticationUtil;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.dto.layerapi.CollectionOwner.*;

@Service
@Order
@RequiredArgsConstructor
public class LayersApiProvider implements LayerProvider {

    private final LayersApiClient layersApiClient;

    @Override
    @Timed(value = "layers.obtainGlobalLayers",  histogram = true)
    public CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(obtainLayersByGeometry(null, searchParams.getAppId()));
    }

    @Override
    @Timed(value = "layers.obtainUserLayers", histogram = true)
    public CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams) {
        if (AuthenticationUtil.isUserAuthenticated()) {
            return CompletableFuture.completedFuture(obtainAllUserOwnedLayers(searchParams.getAppId()));
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    @Override
    @Timed(value = "layers.obtainSelectedAreaLayers", histogram = true)
    public CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams) {
        if (AuthenticationUtil.isUserAuthenticated()) {
            return CompletableFuture.completedFuture(
                    obtainNonUserOwnedLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
        } else {
            return CompletableFuture.completedFuture(
                    obtainLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
        }
    }

    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        return layersApiClient.getLayer(searchParams.getBoundary(), layerId, searchParams.getAppId());
    }

    @Override
    public boolean isApplicable(String layerId) {
        return true;
    }

    private List<Layer> obtainLayersByGeometry(Geometry geoJSON, UUID appId) {
        boolean omitLocalLayers = geoJSON == null;
        return layersApiClient.findLayers(geoJSON, omitLocalLayers, ANY, appId);
    }

    private List<Layer> obtainNonUserOwnedLayersByGeometry(Geometry geoJSON, UUID appId) {
        boolean omitLocalLayers = geoJSON == null;
        return layersApiClient.findLayers(geoJSON, omitLocalLayers, NOT_ME, appId);
    }

    private List<Layer> obtainAllUserOwnedLayers(UUID appId) {
        return layersApiClient.findLayers(null, false, ME, appId);
    }
}
