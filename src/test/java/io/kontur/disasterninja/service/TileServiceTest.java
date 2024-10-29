package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
    public void getBivariateTileMvtTest() {
        ResponseEntity<byte[]> result = ResponseEntity.ok().body(new byte[100]);
        new Random().nextBytes(result.getBody());
        Integer z = 4;
        Integer x = 8;
        Integer y = 6;

        when(insightsApiClient.getBivariateTileMvt(z, x, y, "all", null)).thenReturn(result);

        byte[] tile = service.getBivariateTileMvt(z, x, y, "all", null).getBody();

        verify(insightsApiClient).getBivariateTileMvt(z, x, y, "all", null);
        assertEquals(100, tile.length);
    }
}
