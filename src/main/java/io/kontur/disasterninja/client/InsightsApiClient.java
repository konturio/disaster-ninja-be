package io.kontur.disasterninja.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

@Component
@RequiredArgsConstructor
public class InsightsApiClient {

    private static final String INSIGHTS_API_HUM_IMPACT_URI = "/population/humanitarian_impact";

    private static final String INSIGHTS_API_TILE_MVT_URI = "/tiles/bivariate/v1/%s/%s/%s.mvt";

    private final RestTemplate insightsApiRestTemplate;

    public FeatureCollection getUrbanCoreAndSettledPeripheryLayers(Geometry geoJSON) {
        return insightsApiRestTemplate.postForEntity(INSIGHTS_API_HUM_IMPACT_URI,
            geoJSON, FeatureCollection.class).getBody();
    }

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y) {
        return insightsApiRestTemplate.getForEntity(String.format(INSIGHTS_API_TILE_MVT_URI, z, x, y), byte[].class)
                .getBody();
    }
}
