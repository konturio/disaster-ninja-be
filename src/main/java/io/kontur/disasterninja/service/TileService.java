package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TileService {

    private final InsightsApiClient insightsApiClient;

    public ResponseEntity<byte[]> getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass){
        return insightsApiClient.getBivariateTileMvt(z, x, y, indicatorsClass);
    }
}
