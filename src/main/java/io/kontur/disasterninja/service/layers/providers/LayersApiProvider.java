package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.Layer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
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
    public List<Layer> obtainLayers(Geometry geoJSON, UUID eventId) {
        if (isUserAuthenticated()) {
            List<Layer> result = new ArrayList<>();
            result.addAll(obtainNonUserOwnedLayersByGeometry(geoJSON));
            result.addAll(obtainAllUserOwnedLayers());
            return result;
        } else {
            return obtainLayersByGeometry(geoJSON);
        }
    }

    @Override
    public Layer obtainLayer(Geometry geoJSON, String layerId, UUID eventId) {
        if (!isApplicable(layerId)) {
            return null;
        }
        return layersApiClient.getLayer(geoJSON, layerId);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return layerId.startsWith(LAYER_PREFIX);
    }

    private List<Layer> obtainLayersByGeometry(Geometry geoJSON) {
        return layersApiClient.findLayers(geoJSON, ANY);
    }

    private List<Layer> obtainNonUserOwnedLayersByGeometry(Geometry geoJSON) {
        return layersApiClient.findLayers(geoJSON, NOT_ME);
    }

    private List<Layer> obtainAllUserOwnedLayers() {
        return layersApiClient.findLayers(null, ME);
    }

    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof BearerTokenAuthenticationToken;
    }

}
