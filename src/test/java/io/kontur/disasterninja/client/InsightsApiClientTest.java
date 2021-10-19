package io.kontur.disasterninja.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import k2layers.api.model.GeometryGeoJSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.FeatureCollection;

import java.io.IOException;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Import(TestConfig.class)
@RestClientTest(InsightsApiClient.class)
@AutoConfigureWebClient
class InsightsApiClientTest {

    @Autowired
    @Qualifier("insightsApiRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private InsightsApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void testGetFeatures() throws IOException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        //given
        server.expect(r -> assertThat(r.getURI().toString(),
                equalTo("https://test-apps02.konturlabs.com/insights-api/population/humanitarian_impact")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(r -> assertThat(r.getBody().toString(), equalTo(json)))
            .andRespond(withSuccess(readFile(this, "layers/population.json"),
                MediaType.APPLICATION_JSON));

        //when
        FeatureCollection events = client.getUrbanCoreAndSettledPeripheryLayers(new ObjectMapper().readValue(json,
            GeometryGeoJSON.class));

        //then
        assertEquals(2, events.getFeatures().length);
    }
}