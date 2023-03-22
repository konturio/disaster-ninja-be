package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class TileService {

    private final InsightsApiClient insightsApiClient;

    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass){
        return insightsApiClient.getBivariateTileMvt(z, x, y, indicatorsClass);
    }

    public URI getTilesLocationUri(Integer z, Integer x, Integer y, String indicatorsClass) {
        return insightsApiClient.getTilesLocationUri(z, x, y, indicatorsClass);
    }
}
