package io.kontur.disasterninja.client;

public class InsightsApiClientDummy implements InsightsApiClient {

    @Override
    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        return new byte[0];
    }
}
