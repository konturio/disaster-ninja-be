package io.kontur.disasterninja.client;

import k2layers.api.model.GeometryGeoJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;

@Component
public class InsightsApiClient {
    @Autowired
    @Qualifier("insightsApiRestTemplate")
    RestTemplate restTemplate;

    @Value("${kontur.platform.insightsApi.url}")
    private String insightsApiUrl;

    public FeatureCollection getUrbanCoreAndSettledPeripheryLayers(GeometryGeoJSON geoJSON) {
        String url = insightsApiUrl + "/population/humanitarian_impact";
        return restTemplate.postForEntity(url, geoJSON, FeatureCollection.class).getBody();
    }
}
