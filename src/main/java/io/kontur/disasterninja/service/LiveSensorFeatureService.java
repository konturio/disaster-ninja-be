package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class LiveSensorFeatureService {

    private final LayersApiClient layersApiClient;

    public void appendLiveSensorData(FeatureCollection fc) {
        String user = AuthenticationUtil.getAuthenticatedUser();
        Feature[] features = Arrays.stream(fc.getFeatures())
                .filter(f -> f.getProperties() != null && f.getGeometry() != null)
                .peek(f -> f.getProperties().put("userId", user))
                .toArray(Feature[]::new);

        layersApiClient.appendLayerFeaturesWithDefaultUser("live-sensor", new FeatureCollection(features));
    }

}
