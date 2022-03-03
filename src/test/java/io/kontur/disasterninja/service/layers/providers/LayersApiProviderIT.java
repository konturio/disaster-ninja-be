package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Geometry;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.dto.layerapi.CollectionOwner.ME;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LayersApiProviderIT extends TestDependingOnUserAuth {

    private MockRestServiceServer server;
    @Autowired
    private LayersApiProvider provider;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RestTemplate layersApiRestTemplate;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.createServer(layersApiRestTemplate);
        givenJwtTokenIs(jwt);
    }

    @Test
    public void getLayers() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(3), requestTo("/collections/search"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + jwt))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.collectionOwner", is(ME.name())).matches(body)) {
                        return withSuccess(readFile(this, "/io/kontur/disasterninja/client/layers/layersAPI.layers.ownedByUser.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                    } else if (hasJsonPath("$.offset", is(0)).matches(body)) {
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
        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        List<Layer> layers = provider.obtainLayers(LayerSearchParams.builder().boundary(geometry).build());

        //then
        assertEquals(13, layers.size());

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

        Layer userLayer = layers.get(12);
        assertEquals("KLA__userLayer1", userLayer.getId());
        assertEquals("user layer title", userLayer.getName());
        assertNull(userLayer.getDescription());
        assertNull(userLayer.getCategory());
        assertNull(userLayer.getGroup());
        assertNull(userLayer.getCopyrights());
        assertEquals(LegendType.SIMPLE, userLayer.getLegend().getType());
        assertEquals("sl1", userLayer.getLegend().getSteps().get(0).getSourceLayer());
        assertFalse(userLayer.isBoundaryRequiredForRetrieval());
        assertTrue(userLayer.isOwnedByUser());
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
        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        Layer layer = provider.obtainLayer("KLA__hotProjects", LayerSearchParams.builder()
            .boundary(geometry).build());

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
        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        Layer layer = provider.obtainLayer("KLA__hotProjects_feature_type", LayerSearchParams.builder()
            .boundary(geometry).build());

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
