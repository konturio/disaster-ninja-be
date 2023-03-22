package io.kontur.disasterninja.client;

import java.net.URI;

public interface InsightsApiClient {

    byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass);

    URI getTilesLocationUri(Integer z, Integer x, Integer y, String indicatorsClass);

}
