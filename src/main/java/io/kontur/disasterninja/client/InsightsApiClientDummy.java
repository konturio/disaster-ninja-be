package io.kontur.disasterninja.client;

import java.net.URI;

public class InsightsApiClientDummy implements InsightsApiClient {

    @Override
    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        return new byte[0];
    }

    @Override
    public URI getTilesLocationUri(Integer z, Integer x, Integer y, String indicatorsClass) {
        return null;
    }
}
