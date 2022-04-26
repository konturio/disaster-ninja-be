package io.kontur.disasterninja.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.MultiPolygon;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.client.KcApiClient.HOT_PROJECTS;
import static io.kontur.disasterninja.client.KcApiClient.OSM_LAYERS;
import static io.kontur.disasterninja.dto.layer.LayerUpdateDto.LAYER_TYPE_FEATURE;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(KcApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class KcApiClientTest {

    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private KcApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void collectionItemsByCentroidGeometryTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[-1.0,-1.0],[-1.0,1.0],[1.0,1.0],[1.0,-1.0],[-1.0,-1.0]]]}";

        //given
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(), containsString(
                "/collections/hotProjects/itemsByGeometry")))
            .andExpect(method(HttpMethod.POST))
            .andRespond(request -> {
                String body = request.getBody().toString();

                if (!hasJsonPath("$.geom", is(json)).matches(body)) {
                    return withStatus(HttpStatus.BAD_REQUEST).createResponse(request);
                }

                return withSuccess(readFile(this, "layers/hotprojects.json")
                                .replaceAll("\"numberMatched\": 8305,", "\"numberMatched\": 10,")
                                .replaceAll("\"numberReturned\": 10", "\"numberReturned\": 10"),
                        MediaType.APPLICATION_JSON).createResponse(request);
            });

        //when
        List<Feature> features = client.getCollectionItemsByCentroidGeometry(objectMapper.readValue(json,
            Geometry.class), HOT_PROJECTS);

        //then
        assertEquals(1, features.size());
        Feature feature = features.get(0);
        assertTrue(feature.getGeometry() instanceof Point);
        assertArrayEquals(new double[]{-0.25d, -0.25d}, ((Point) feature.getGeometry()).getCoordinates());
    }

    @Test
    public void collectionByGeometryOnePageTest() throws IOException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        //given
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(), containsString(
                        "/collections/osmlayer/itemsByGeometry")))
            .andExpect(method(HttpMethod.POST))
            .andRespond(request -> {
                String body = request.getBody().toString();

                if (!hasJsonPath("$.geom", is(json)).matches(body)) {
                    return withStatus(HttpStatus.BAD_REQUEST).createResponse(request);
                }

                //first page request
                if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "layers/osmlayer.json"),
                            MediaType.APPLICATION_JSON).createResponse(request);
                    }
                    //no more requests expected
                    throw new RuntimeException("incorrect request uri!");
                }
            );

        //when
        List<Feature> events = client.getCollectionItemsByGeometry(objectMapper.readValue(json,
            Geometry.class), "osmlayer");

        //then
        assertEquals(10, events.size());
    }

    @Test
    public void collectionByGeometryThreePagesTest() throws IOException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

        //given
        server.expect(ExpectedCount.times(3), r -> assertThat(r.getURI().toString(), matchesRegex(
                        "/collections/osmlayer/itemsByGeometry")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    String body = request.getBody().toString();

                    if (!hasJsonPath("$.geom", is(json)).matches(body)) {
                        return withStatus(HttpStatus.BAD_REQUEST).createResponse(request);
                    }

                    //first page request
                    if (hasJsonPath("$.offset", is(0)).matches(body)) {
                        return withSuccess(readFile(this, "layers/osmlayer.json")
                                        .replaceAll("\"numberMatched\": 10,", "\"numberMatched\": 22,"),
                                MediaType.APPLICATION_JSON).createResponse(request);
                    }
                    //second page request
                    if (hasJsonPath("$.offset", is(10)).matches(body)) {

                        return withSuccess(readFile(this, "layers/osmlayer.json")
                                        .replaceAll("\"numberMatched\": 10,", "\"numberMatched\": 22,"),
                                MediaType.APPLICATION_JSON).createResponse(request);
                    }
                    //third page request (just 2 features)
                    if (hasJsonPath("$.offset", is(20)).matches(body)) {

                        return withSuccess(readFile(this, "layers/osmlayer_2.json"),
                                MediaType.APPLICATION_JSON).createResponse(request);
                    }
                    throw new RuntimeException("incorrect request uri!");
                });

        //when
        List<Feature> events = client.getCollectionItemsByGeometry(objectMapper.readValue(json,
            Geometry.class), "osmlayer");

        //then
        assertEquals(22, events.size());
    }

    @Test
    public void collectionThreePagesTest() throws JsonProcessingException {
        String json = "{\"type\":\"Polygon\",\"coordinates\":[[[-1.0,-1.0],[-1.0,1.0],[1.0,1.0],[1.0,-1.0],[-1.0,-1.0]]]}";

        //given
        server.expect(ExpectedCount.times(3), r -> assertThat(r.getURI().toString(), matchesRegex(
                "/collections/osmlayer/itemsByGeometry")))
            .andExpect(method(HttpMethod.POST))
            .andRespond(request -> {
                String body = request.getBody().toString();

                if (!hasJsonPath("$.geom", is(json)).matches(body)) {
                    return withStatus(HttpStatus.BAD_REQUEST).createResponse(request);
                }

                //first page request
                if (hasJsonPath("$.offset", is(0)).matches(body)) {

                    return withSuccess(readFile(this, "layers/osmlayer.json")
                            .replaceAll("\"numberMatched\": 10,", "\"numberMatched\": 22,"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
                //second page request
                if (hasJsonPath("$.offset", is(10)).matches(body)) {
                    return withSuccess(readFile(this, "layers/osmlayer.json")
                            .replaceAll("\"numberMatched\": 10,", "\"numberMatched\": 22,"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
                //third page request (just 2 features)
                if (hasJsonPath("$.offset", is(20)).matches(body)) {
                    return withSuccess(readFile(this, "layers/osmlayer_2.json"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
                throw new RuntimeException("incorrect request uri!");
            });

        //when
        List<Feature> events = client.getCollectionItems("osmlayer", objectMapper.readValue(json,
                Geometry.class));

        //then
        assertEquals(22, events.size());
    }

    @Test
    public void singleFeatureFromCollectionTest() {
        //given
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(), endsWith(
                "/collections/osmlayer/items/Benin_cotonou_pleiade_2016")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(request -> {
                    return withSuccess(readFile(this, "layers/osmlayer_feature.json"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
            );

        //when
        Feature event = client.getFeatureFromCollection(null, "Benin_cotonou_pleiade_2016",
            OSM_LAYERS); //not limited by geoJson

        //then
        assertEquals("Benin_cotonou_pleiade_2016", event.getId());
        assertEquals(MultiPolygon.class, event.getGeometry().getClass());
        assertEquals(2, ((MultiPolygon) event.getGeometry()).getCoordinates().length);
    }

    @Test
    public void singleFeatureFromCollectionWithBoundaryTest() {
        //given
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(), endsWith(
                "/collections/osmlayer/items/Benin_cotonou_pleiade_2016")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(request -> {
                    return withSuccess(readFile(this, "layers/osmlayer_feature.json"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
            );

        //when geoJson intersects with layer
        Geometry geoJson = new Point(new double[]{1.722946974, 6.266307793});
        Feature event = client.getFeatureFromCollection(geoJson, "Benin_cotonou_pleiade_2016", OSM_LAYERS); //limited by geoJson

        //then
        assertEquals("Benin_cotonou_pleiade_2016", event.getId());
        assertEquals(MultiPolygon.class, event.getGeometry().getClass());
        assertEquals(2, ((MultiPolygon) event.getGeometry()).getCoordinates().length);
    }

    @Test
    public void singleFeatureFromCollectionWithNotIntersectingBoundaryTest() {
        //given
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(), endsWith(
                "/collections/osmlayer/items/Benin_cotonou_pleiade_2016")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(request -> {
                    return withSuccess(readFile(this, "layers/osmlayer_feature.json"),
                        MediaType.APPLICATION_JSON).createResponse(request);
                }
            );

        //when
        //1 geoJson intersects with layer
        Geometry geoJson = new Point(new double[]{0, 0});
        Feature event = client.getFeatureFromCollection(geoJson, "Benin_cotonou_pleiade_2016", OSM_LAYERS); //limited by geoJson

        //then
        assertNull(event);
    }
}