package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.client.LayersApiClient.LAYER_PREFIX;
import static io.kontur.disasterninja.dto.layerapi.CollectionOwner.*;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LayersApiProvider implements LayerProvider {

    private final LayersApiClient layersApiClient;

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    // TODO: retained for backward compatibility, remove later
    public CompletableFuture<List<Layer>> obtainLayers(LayerSearchParams searchParams) {
        if (isUserAuthenticated()) {
            List<Layer> result = new ArrayList<>();
            result.addAll(obtainNonUserOwnedLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
            result.addAll(obtainAllUserOwnedLayers(searchParams.getAppId()));
            return CompletableFuture.completedFuture(result);
        } else {
            return CompletableFuture.completedFuture(
                    obtainLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
        }
    }

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(obtainLayersByGeometry(null, null));
    }

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(obtainAllUserOwnedLayers(searchParams.getAppId()));
    }

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams) {
        if (isUserAuthenticated()) {
            return CompletableFuture.completedFuture(
                    obtainNonUserOwnedLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
        } else {
            return CompletableFuture.completedFuture(
                    obtainLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
        }
    }

    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        return layersApiClient.getLayer(searchParams.getBoundary(), layerId, searchParams.getAppId());
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
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

    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JwtAuthenticationToken;
    }

}
