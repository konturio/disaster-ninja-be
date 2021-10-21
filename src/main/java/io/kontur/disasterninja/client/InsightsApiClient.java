package io.kontur.disasterninja.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

@Component
public class InsightsApiClient {
    @Autowired
    @Qualifier("insightsApiRestTemplate")
    RestTemplate restTemplate;

    public FeatureCollection getUrbanCoreAndSettledPeripheryLayers(Geometry geoJSON) {
        String url = "/population/humanitarian_impact";
        return restTemplate.postForEntity(url, geoJSON, FeatureCollection.class).getBody();
    }
}
