package io.kontur.disasterninja.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

@Component
public class InsightsApiClient {
    @Autowired
    RestTemplate insightsApiRestTemplate;

    public FeatureCollection getUrbanCoreAndSettledPeripheryLayers(Geometry geoJSON) {
        String url = "/population/humanitarian_impact";
        return insightsApiRestTemplate.postForEntity(url, geoJSON, FeatureCollection.class).getBody();
    }
}
