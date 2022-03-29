package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
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

import static io.kontur.disasterninja.client.LayersApiClient.LAYER_PREFIX;
import static io.kontur.disasterninja.dto.layerapi.CollectionOwner.*;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LayersApiProvider implements LayerProvider {

    private final LayersApiClient layersApiClient;

    @Override
    public List<Layer> obtainLayers(LayerSearchParams searchParams) {
        if (isUserAuthenticated()) {
            List<Layer> result = new ArrayList<>();
            result.addAll(obtainNonUserOwnedLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId()));
            result.addAll(obtainAllUserOwnedLayers(searchParams.getAppId()));
            return result;
        } else {
            return obtainLayersByGeometry(searchParams.getBoundary(), searchParams.getAppId());
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
        return layersApiClient.findLayers(geoJSON, ANY, appId);
    }

    private List<Layer> obtainNonUserOwnedLayersByGeometry(Geometry geoJSON, UUID appId) {
        return layersApiClient.findLayers(geoJSON, NOT_ME, appId);
    }

    private List<Layer> obtainAllUserOwnedLayers(UUID appId) {
        return layersApiClient.findLayers(null, ME, appId);
    }

    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JwtAuthenticationToken;
    }

}
