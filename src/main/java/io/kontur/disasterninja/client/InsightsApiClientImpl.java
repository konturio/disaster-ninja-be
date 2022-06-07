package io.kontur.disasterninja.client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
public class InsightsApiClientImpl implements InsightsApiClient {

    private static final String INSIGHTS_API_TILE_MVT_URI = "/tiles/bivariate/v1/%s/%s/%s.mvt?indicatorsClass=%s";

    private final RestTemplate insightsApiRestTemplate;

    @Override
    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        return insightsApiRestTemplate.getForEntity(String.format(INSIGHTS_API_TILE_MVT_URI, z, x, y, indicatorsClass), byte[].class)
                .getBody();
    }
}
