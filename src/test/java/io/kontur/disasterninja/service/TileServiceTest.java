package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TileServiceTest {

    @Mock
    private InsightsApiClient insightsApiClient;

    @InjectMocks
    private TileService service;

    @Test
    public void getTileMvtTest() {
        byte[] result = new byte[100];
        new Random().nextBytes(result);
        Integer z = 4;
        Integer x = 8;
        Integer y = 6;

        when(insightsApiClient.getTileMvt(z, x, y)).thenReturn(result);

        byte[] tile = service.getTileMvt(z, x, y);

        verify(insightsApiClient).getTileMvt(z, x, y);
        assertEquals(100, tile.length);
    }
}