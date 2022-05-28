package io.kontur.disasterninja.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.util.Random;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(InsightsApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class InsightsApiClientTest {

    @Autowired
    private InsightsApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void testGetFeatures() throws IOException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        //given
        server.expect(r -> assertThat(r.getURI().toString(),
                equalTo("/population/humanitarian_impact")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(r -> assertThat(r.getBody().toString(), equalTo(json)))
            .andRespond(withSuccess(readFile(this, "layers/population.json"),
                MediaType.APPLICATION_JSON));

        //when
        FeatureCollection events = client.getUrbanCoreAndSettledPeripheryLayers(new ObjectMapper().readValue(json,
            Geometry.class));

        //then
        assertEquals(2, events.getFeatures().length);
    }

    @Test
    public void testGetTileMvt(){
        byte[] result = new byte[100];
        new Random().nextBytes(result);
        Integer z = 4;
        Integer x = 8;
        Integer y = 6;

        //given
        server.expect(r -> assertThat(r.getURI().toString(),
                        equalTo(String.format("/tiles/%s/%s/%s.mvt", z, x, y))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(result,
                        MediaType.parseMediaType("application/vnd.mapbox-vector-tile")));

        //when
        byte[] tile = client.getTileMvt(z, x, y);

        //then
        assertEquals(100, tile.length);
    }
}