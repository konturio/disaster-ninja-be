package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layerapi.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.*;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.client.LayersApiClient.LAYER_PREFIX;
import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_FEATURE;
import static io.kontur.disasterninja.util.TestUtil.createLegend;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LayersApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class LayersApiClientTest extends TestDependingOnUserAuth {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private LayersApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @BeforeEach
    public void beforeEach() {
        givenUserIsLoggedIn();
    }

    @Test
    public void createLayerTest() {
        server.expect(ExpectedCount.times(1), requestTo("/collections"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.itemType", is(LAYER_TYPE_FEATURE)))
                .andRespond(r ->
                        withSuccess(readFile(this, "layers/layersAPI.layer.created.json"),
                                MediaType.APPLICATION_JSON)
                                .createResponse(r)
                );

        final String id = "myId";
        final String title = "layer title";
        Legend legend = createLegend();

        LayerCreateDto dto = new LayerCreateDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setLegend(legend);

        Layer layer = client.createLayer(dto);
        assertEquals(LAYER_PREFIX + id, layer.getId());
        assertEquals(title, layer.getName());
        assertEquals(legend, layer.getLegend());
        assertTrue(layer.isOwnedByUser());
    }

    @Test
    public void createLayerNegativeTest() {
        server.expect(ExpectedCount.times(1), requestTo("/collections"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.BadRequest.class, () -> client.createCollection(null));
    }

    @Test
    public void updateLayerTest() {
        final String id = "myId";
        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.itemType", is(LAYER_TYPE_FEATURE)))
                .andRespond(r ->
                        withSuccess(readFile(this, "layers/layersAPI.layer.created.json"),
                                MediaType.APPLICATION_JSON)
                                .createResponse(r)
                );

        final String title = "layer title";
        Legend legend = createLegend();

        LayerUpdateDto dto = new LayerUpdateDto();
        dto.setTitle(title);
        dto.setLegend(legend);

        Layer layer = client.updateLayer(LAYER_PREFIX + id, dto);
        assertEquals(LAYER_PREFIX + id, layer.getId());
        assertEquals(title, layer.getName());
        assertEquals(legend, layer.getLegend());
        assertTrue(layer.isOwnedByUser());
    }

    @Test
    public void updateLayerNegativeTest() {
        final String id = "myId";
        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.NotFound.class, () -> client.updateCollection(id, null));
    }

    @Test
    public void deleteLayerTest() {
        final String id = "myId";
        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        client.deleteLayer(LAYER_PREFIX + id);
    }

    @Test
    public void deleteLayerNegativeTest() {
        final String id = "myId";
        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(Exception.class, () -> client.deleteCollection(id));
    }

    @Test
    public void thereIsCacheControlNoCacheHeaderInRequest() {
        final String id = "myId";
        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue()))
                .andRespond(withSuccess());

        client.deleteLayer(LAYER_PREFIX + id);
    }

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
        List<Collection> collections = client.getCollections(objectMapper.readValue(json, Geometry.class), null, null);

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
        assertEquals(LegendType.SIMPLE.toString(), collection.getStyleRule().get("type").textValue());
    }

    @Test
    public void getCollectionsEmptyResponse() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";
        UUID appId = UUID.randomUUID();

        server.expect(ExpectedCount.times(2), requestTo("/collections/search"))
                .andExpect(request -> {
                    String s = request.getBody().toString();
                    assertThat(s, hasJsonPath("$.appId", is(appId.toString())));
                })
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> withSuccess().createResponse(r));
        //when
        List<Collection> collections = client.getCollections(objectMapper.readValue(json, Geometry.class),
                null, appId);

        //then
        assertEquals(0, collections.size());
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
                    if (hasJsonPath("$.appId", is(not("a2a353ab-4126-44ef-a25d-a93fee401c1f"))).matches(body)) {
                        throw new RuntimeException();
                    }
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
        List<Feature> features = client.getCollectionFeatures(objectMapper.readValue(json, Geometry.class),
                "hotProjects", UUID.fromString("a2a353ab-4126-44ef-a25d-a93fee401c1f"));

        //then
        assertEquals(12, features.size());

        Feature feature = features.get(0);
        assertEquals("100", feature.getId());
        assertEquals("ARCHIVED", feature.getProperties().get("status"));
        assertEquals("MultiPolygon", feature.getGeometry().getType());
    }

    @Test
    public void getCollectionFeaturesWithoutAppId() throws JsonProcessingException {
        //given
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
                "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        server.expect(ExpectedCount.times(1), requestTo("/collections/hotProjects/items/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.appId").matches(body)) {
                        throw new RuntimeException();
                    }
                    return withSuccess().createResponse(r);
                });

        //when
        List<Feature> features = client.getCollectionFeatures(objectMapper.readValue(json, Geometry.class),
                "hotProjects", null);

        //then
        assertEquals(0, features.size());
    }

    @Test
    public void getCollection() {
        //given
        UUID appId = UUID.randomUUID();

        server.expect(ExpectedCount.times(1), requestTo("/collections/search"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(r -> {
                    String body = r.getBody().toString();
                    if (hasJsonPath("$.collectionIds", hasSize(1)).matches(body) &&
                            hasJsonPath("$.collectionIds", contains("hotProjects")).matches(body) &&
                            hasJsonPath("$.appId", is(appId.toString())).matches(body)) {
                        return withSuccess(readFile(this, "layers/layersAPI.search.hotProjects.response.json"),
                                MediaType.APPLICATION_JSON).createResponse(r);
                    } else {
                        throw new RuntimeException();
                    }
                });

        //when
        Collection collection = client.getCollection("hotProjects", appId);

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
        assertEquals(LegendType.SIMPLE.toString(), collection.getStyleRule().get("type").textValue());
    }

    @Test
    public void updateLayerFeaturesTest() throws JsonProcessingException {
        //GIVEN
        final String id = "layerId";
        final String bodyJson = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"name\":\"feature1\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[41.1328125,27.059125784374068]}},{\"type\":\"Feature\",\"properties\":{\"name\":\"feature2\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[43.2421875,47.754097979680026]}}]}";
        FeatureCollection body = new ObjectMapper().readValue(bodyJson, FeatureCollection.class);

        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id + "/items"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(
                        r -> assertThat(r.getBody().toString(), equalTo(new ObjectMapper().writeValueAsString(body))))
                .andRespond(r ->
                        withSuccess(readFile(this, "layers/layersAPI.features.updated.json"),
                                MediaType.APPLICATION_JSON)
                                .createResponse(r)
                );

        //WHEN
        FeatureCollection result = client.updateLayerFeatures(id, body);

        //THEN
        assertEquals(2, result.getFeatures().length);
        assertEquals("jlayRsW2", result.getFeatures()[0].getId());
        assertEquals("msjYHng9", result.getFeatures()[1].getId());
    }

    @Test
    public void updateLayerFeaturesTest_Negative() {
        //GIVEN
        final String id = "layerId";

        server.expect(ExpectedCount.times(1), requestTo("/collections/" + id + "/items"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //WHEN
        //THEN
        assertThrows(HttpClientErrorException.NotFound.class,
                () -> client.updateLayerFeatures(id, new FeatureCollection(new Feature[0])));
    }
}