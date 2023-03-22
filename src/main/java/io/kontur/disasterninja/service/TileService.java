package io.kontur.disasterninja.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class TileService {

    @Value("${kontur.platform.insightsApi.url}")
    private String insightsApiHost;

    private static final String INSIGHTS_API_TILES_PATH = "/tiles/bivariate/v1/%s/%s/%s.mvt?indicatorsClass=%s";

    public URI getTilesLocationUri(Integer z, Integer x, Integer y, String indicatorsClass) {
        return URI.create(insightsApiHost + String.format(INSIGHTS_API_TILES_PATH, z, x, y, indicatorsClass));
    }
}
