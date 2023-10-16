package io.kontur.disasterninja.client;

import org.springframework.http.ResponseEntity;

public interface InsightsApiClient {

    ResponseEntity<byte[]> getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass);

    ResponseEntity<byte[]> getBivariateTileMvtV2(Integer z, Integer x, Integer y, String indicatorsClass);
}
