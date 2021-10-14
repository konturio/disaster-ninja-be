package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.domain.Layer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.GeoJSON;

import java.util.ArrayList;

@Service
public class LayerService {
    @Autowired
    KcApiClient kcApiClient;
    @Autowired
    InsightsApiClient insightsApiClient;

    public ArrayList<Layer> getList(GeoJSON geoJSON, String eventId) {
        return new ArrayList<>(); //todo
    }

    private void getOsmLayers() {
    }

    private void getHotProjectLayers() {
    }

    private void getUrbanCodeAndPeripheryLayers() {
    }
}
