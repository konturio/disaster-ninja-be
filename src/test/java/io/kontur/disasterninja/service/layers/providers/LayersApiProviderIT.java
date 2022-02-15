package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LayersApiProviderIT {

    private MockRestServiceServer server;
    @MockBean
    KeycloakAuthorizationService authorizationService;
    @Autowired
    private LayersApiProvider provider;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RestTemplate layersApiRestTemplate;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.createServer(layersApiRestTemplate);
    }

    @Test
    public void getLayers() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(2), requestTo("/collections/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.layers.page1.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else if (hasJsonPath("$.offset", is(10)).matches(body)) {
                        return withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.layers.page2.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else {
                        throw new RuntimeException();
                    }

                });

        //when
        List<Layer> layers = provider.obtainLayers(objectMapper.readValue(json, Geometry.class), UUID.randomUUID());

        //then
        assertEquals(12, layers.size());

        Layer layer = layers.get(0);
        assertEquals("KLA__hotProjects", layer.getId());
        assertEquals("HOT Tasking Manager Projects", layer.getName());
        assertEquals("Projects on HOT Tasking manager, ongoing and historical", layer.getDescription());
        assertEquals(LayerCategory.OVERLAY, layer.getCategory());
        assertEquals("group_1", layer.getGroup());
        assertEquals("(c) Kontur", layer.getCopyrights().get(0));
        assertEquals(LegendType.SIMPLE, layer.getLegend().getType());
        assertEquals("hotProjects", layer.getLegend().getSteps().get(0).getSourceLayer());
        assertFalse(layer.isBoundaryRequiredForRetrieval());
    }

    @Test
    public void getVectorLayerDetails() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(1), requestTo("/collections/hotProjects"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(r -> withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.layer.vector.json"),
                        MediaType.APPLICATION_JSON).createResponse(r));

        //when
        Layer layer = provider.obtainLayer(objectMapper.readValue(json, Geometry.class), "KLA__hotProjects", UUID.randomUUID());

        //then
        assertEquals("KLA__hotProjects", layer.getId());
        assertNotNull(layer.getSource());
        LayerSource source = layer.getSource();
        assertEquals(LayerSourceType.VECTOR, source.getType());
        assertEquals(1, source.getUrls().size());
        assertEquals("https://test-api02.konturlabs.com/tiles/public.hot_projects/{z}/{x}/{y}.pbf", source.getUrls().get(0));
        assertNull(source.getData());
    }

    @Test
    public void getGeoJsonLayerDetails() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(1), requestTo("/collections/hotProjects_feature_type"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(r -> withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.layer.geojson.json"),
                        MediaType.APPLICATION_JSON).createResponse(r));

        server.expect(ExpectedCount.times(2), requestTo("/collections/hotProjects_feature_type/items/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.features.page1.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else if (hasJsonPath("$.offset", is(10)).matches(body)) {
                        return withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.features.page2.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else {
                        throw new RuntimeException();
                    }

                });

        //when
        Layer layer = provider.obtainLayer(objectMapper.readValue(json, Geometry.class), "KLA__hotProjects_feature_type", UUID.randomUUID());

        //then
        assertEquals("KLA__hotProjects_feature_type", layer.getId());
        assertNotNull(layer.getSource());
        LayerSource source = layer.getSource();
        assertEquals(LayerSourceType.GEOJSON, source.getType());
        assertNull(source.getUrls());
        assertNotNull(source.getData());
        assertEquals(12, source.getData().getFeatures().length);
        assertEquals("100", source.getData().getFeatures()[0].getId());
    }


}
