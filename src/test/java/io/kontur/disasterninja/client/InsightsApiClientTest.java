package io.kontur.disasterninja.client;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(InsightsApiClientImpl.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class InsightsApiClientTest {

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private RestTemplate insightsApiRestTemplate;

    @MockBean
    private MeterRegistry meterRegistry;

    private InsightsApiClient client;

    @BeforeEach
    public void before() {
        client = new InsightsApiClientImpl(insightsApiRestTemplate, meterRegistry);
    }

    @Test
    public void testGetBivariateTileMvt() {
        byte[] result = new byte[100];
        new Random().nextBytes(result);
        Integer z = 4;
        Integer x = 8;
        Integer y = 6;

        //given
        server.expect(r -> assertThat(r.getURI().toString(),
                        equalTo(String.format("/tiles/bivariate/v1/%s/%s/%s.mvt?indicatorsClass=all", z, x, y))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(result,
                        MediaType.parseMediaType("application/vnd.mapbox-vector-tile")));

        //when
        byte[] tile = client.getBivariateTileMvt(z, x, y, "all", null).getBody();

        //then
        assertNotNull(tile);
        assertEquals(100, tile.length);
    }

    @Test
    public void testGetBivariateTileMvtV2() {
        byte[] result = new byte[100];
        new Random().nextBytes(result);
        Integer z = 4;
        Integer x = 8;
        Integer y = 6;

        //given
        server.expect(r -> assertThat(r.getURI().toString(),
                        equalTo(String.format("/tiles/bivariate/v2/%s/%s/%s.mvt?indicatorsClass=all", z, x, y))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(result,
                        MediaType.parseMediaType("application/vnd.mapbox-vector-tile")));

        //when
        byte[] tile = client.getBivariateTileMvtV2(z, x, y, "all").getBody();

        //then
        assertNotNull(tile);
        assertEquals(100, tile.length);
    }
}
