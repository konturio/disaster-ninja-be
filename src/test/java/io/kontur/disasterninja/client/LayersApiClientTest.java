package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layerapi.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LayersApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class LayersApiClientTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private LayersApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void getCollectionsWithPagination() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(2), requestTo("/collections/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "layers/layersAPI.layers.page1.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else if (hasJsonPath("$.offset", is(10)).matches(body)) {
                        return withSuccess(readFile(this, "layers/layersAPI.layers.page2.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else {
                        throw new RuntimeException();
                    }

                });

        //when
        List<Collection> collections = client.getCollections(objectMapper.readValue(json, Geometry.class));

        //then
        assertEquals(12, collections.size());

        Collection collection = collections.get(0);
        assertEquals("hotProjects", collection.getId());
        assertEquals("HOT Tasking Manager Projects", collection.getTitle());
        assertEquals("Projects on HOT Tasking manager, ongoing and historical", collection.getDescription());
        assertEquals("(c) Kontur", collection.getCopyrights());
        assertEquals("overlay", collection.getCategory().getName());
        assertEquals("tiles", collection.getItemType());
        assertEquals("tiles", collection.getItemType());
        assertEquals(1, collection.getLinks().size());
        assertEquals("tiles", collection.getLinks().get(0).getRel());
        assertEquals("https://test-api02.konturlabs.com/tiles/public.hot_projects/{z}/{x}/{y}.pbf",
                collection.getLinks().get(0).getHref());
        assertEquals(LegendType.SIMPLE, collection.getLegend().getType());
    }

    @Test
    public void getCollectionFeaturesWithPagination() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(2), requestTo("/collections/hotProjects/items/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "layers/layersAPI.features.page1.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else if (hasJsonPath("$.offset", is(10)).matches(body)) {
                        return withSuccess(readFile(this, "layers/layersAPI.features.page2.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else {
                        throw new RuntimeException();
                    }

                });

        //when
        List<Feature> features = client.getCollectionFeatures(objectMapper.readValue(json, Geometry.class), "hotProjects");

        //then
        assertEquals(12, features.size());

        Feature feature = features.get(0);
        assertEquals("100", feature.getId());
        assertEquals("ARCHIVED", feature.getProperties().get("status"));
        assertEquals("MultiPolygon", feature.getGeometry().getType());
    }

    @Test
    public void getCollection() {
        //given
        server.expect(ExpectedCount.times(1), requestTo("/collections/hotProjects"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(r -> withSuccess(readFile(this, "layers/layersAPI.layer.vector.json"),
                        MediaType.APPLICATION_JSON).createResponse(r));

        //when
        Collection collection = client.getCollection("hotProjects");

        //then
        assertEquals("hotProjects", collection.getId());
        assertEquals("HOT Tasking Manager Projects", collection.getTitle());
        assertEquals("Projects on HOT Tasking manager, ongoing and historical", collection.getDescription());
        assertEquals("(c) Kontur", collection.getCopyrights());
        assertEquals("test_HotProjectsCategory", collection.getCategory().getName());
        assertEquals("tiles", collection.getItemType());
        assertEquals("tiles", collection.getItemType());
        assertEquals(1, collection.getLinks().size());
        assertEquals("tiles", collection.getLinks().get(0).getRel());
        assertEquals("https://test-api02.konturlabs.com/tiles/public.hot_projects/{z}/{x}/{y}.pbf",
                collection.getLinks().get(0).getHref());
        assertEquals(LegendType.SIMPLE, collection.getLegend().getType());
    }
}