package io.kontur.disasterninja.client;

import org.springframework.http.ResponseEntity;

public class InsightsApiClientDummy implements InsightsApiClient {

    @Override
    public ResponseEntity<byte[]> getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        return ResponseEntity.ok()
                .body(new byte[0]);
    }
}
