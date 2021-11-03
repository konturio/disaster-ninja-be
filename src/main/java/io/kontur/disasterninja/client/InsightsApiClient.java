package io.kontur.disasterninja.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

@Component
public class InsightsApiClient {

    private static final String INSIGHTS_API_HUM_IMPACT_URI = "/population/humanitarian_impact";

    @Autowired
    RestTemplate insightsApiRestTemplate;

    public FeatureCollection getUrbanCoreAndSettledPeripheryLayers(Geometry geoJSON) {
        return insightsApiRestTemplate.postForEntity(INSIGHTS_API_HUM_IMPACT_URI,
            geoJSON, FeatureCollection.class).getBody();
    }
}
