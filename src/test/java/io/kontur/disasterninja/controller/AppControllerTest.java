package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.AppSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpClientErrorException;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.kontur.disasterninja.client.LayersApiClient.LAYER_PREFIX;
import static io.kontur.disasterninja.controller.AppsController.PATH;
import static io.kontur.disasterninja.service.GeometryTransformer.geometriesAreEqual;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = {UserProfileClient.class, LayersApiClient.class})
@AutoConfigureWebClient(registerRestTemplate = true)
public class AppControllerTest extends TestDependingOnUserAuth {

    private static final UUID DN2_ID = UUID.fromString("1b303b14-ddce-42b9-b907-d31ed488e301");
    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private UserProfileClient userProfileClient;
    @Autowired
    private LayersApiClient layersApiClient;
    private AppsController appsController;

    @BeforeEach
    public void before() {
        appsController = new AppsController(userProfileClient, layersApiClient);
    }

    @Test
    public void getAppsListUnauthenticated() throws IOException {
        givenUserIsNotAuthenticated();
        mockServer.expect(requestTo(PATH))
                .andExpect(method(GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess(readFile(this, "ups/publicApps.json"), MediaType.APPLICATION_JSON));

        List<AppSummaryDto> result = appsController.getList();

        assertEquals(1, result.size());
        assertTrue(result.contains(new AppSummaryDto(DN2_ID, "DN2")));
    }

    @Test
    public void getAppsList() throws IOException {
        givenUserIsLoggedIn();
        mockServer.expect(requestTo(PATH))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(withSuccess(readFile(this, "ups/privateApps.json"), MediaType.APPLICATION_JSON));

        List<AppSummaryDto> result = appsController.getList();

        assertEquals(2, result.size());
        assertTrue(result.contains(new AppSummaryDto(DN2_ID, "DN2")));
        assertTrue(result.contains(new AppSummaryDto(UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb11"),
                "my-private-app")));
    }

    @Test
    public void getDefaultAppIdUnauthenticated() {
        String dn2AppId = UUID.randomUUID().toString();
        givenUserIsNotAuthenticated();
        mockServer.expect(requestTo(PATH + "/" + "default_id"))
                .andExpect(method(GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess(dn2AppId, MediaType.TEXT_PLAIN));

        String result = appsController.getDefaultAppId().getBody();
        assertEquals(dn2AppId, result);
    }

    @Test
    public void getAppUnauthenticated() throws IOException {
        givenUserIsNotAuthenticated();
        mockServer.expect(requestTo(PATH + "/" + DN2_ID))
                .andExpect(method(GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess(readFile(this, "ups/dn2App.json"), MediaType.APPLICATION_JSON));

        AppDto result = appsController.get(DN2_ID);

        assertEquals(DN2_ID, result.getId());
        assertEquals("DN2", result.getName());
        assertEquals("Disaster Ninja 2.0", result.getDescription());
        assertFalse(result.getOwnedByUser());
        assertEquals(28, result.getFeatures().size());
        assertNull(result.getCenterGeometry());
        assertNull(result.getZoom());
        assertTrue(result.isPublic());
    }

    @Test
    public void getApp() throws IOException {
        givenUserIsLoggedIn();
        UUID appId = UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb11");
        mockServer.expect(requestTo(PATH + "/" + appId))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(withSuccess(readFile(this, "ups/privateApp.json"), MediaType.APPLICATION_JSON));

        AppDto result = appsController.get(appId);

        assertEquals(appId, result.getId());
        assertEquals("my-private-app", result.getName());
        assertEquals("Disaster Ninja 666.6", result.getDescription());
        assertTrue(result.getOwnedByUser());
        assertEquals(3, result.getFeatures().size());
        assertTrue(result.getFeatures().contains("interactive_map"));
        assertTrue(result.getFeatures().contains("reports"));
        assertTrue(result.getFeatures().contains("url_store"));
        assertTrue(geometriesAreEqual(new Point(new double[]{125.6, 10.1}), result.getCenterGeometry()));
        assertFalse(result.isPublic());
        assertEquals(BigDecimal.valueOf(123.456), result.getZoom());
    }

    @Test
    public void deleteApp() {
        givenUserIsLoggedIn();
        UUID appId = UUID.randomUUID();
        mockServer.expect(requestTo(PATH + "/" + appId))
                .andExpect(method(DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertEquals(HttpStatus.NO_CONTENT, appsController.delete(appId).getStatusCode());
    }

    @Test
    public void updateApp() throws IOException {
        givenUserIsLoggedIn();
        UUID appId = UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb11");

        AppDto update = createAppDto();
        update.setOwnedByUser(true);
        update.setId(appId);
        ObjectMapper objectMapper = new ObjectMapper();
        String dto = objectMapper.writeValueAsString(update);

        mockServer.expect(requestTo(PATH + "/" + appId))
                .andExpect(method(PUT))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andExpect(content().json(dto, true))
                .andRespond(withSuccess(dto, MediaType.APPLICATION_JSON));

        AppDto result = appsController.update(appId, update);
        assertEquals(update, result);
    }

    @Test
    public void createApp() throws IOException {
        givenUserIsLoggedIn();
        ObjectMapper objectMapper = new ObjectMapper();

        AppDto request = createAppDto();
        String requestJson = objectMapper.writeValueAsString(request);

        AppDto response = createAppDto();
        UUID appId = UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb11");
        response.setId(appId);
        response.setOwnedByUser(true);
        String responseJson = objectMapper.writeValueAsString(response);

        mockServer.expect(requestTo(PATH))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andExpect(content().json(requestJson, true))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AppDto result = appsController.create(request);
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getFeatures(), result.getFeatures());
        assertEquals(request.isPublic(), result.isPublic());
        assertTrue(geometriesAreEqual(request.getCenterGeometry(), result.getCenterGeometry()));
        assertEquals(request.getZoom(), result.getZoom());

        assertEquals(appId, result.getId());
        assertEquals(true, result.getOwnedByUser());
    }

    @Test
    public void test404isPassedToTheClient() {
        givenUserIsLoggedIn();
        mockServer.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.NotFound.class, () -> appsController.get(UUID.randomUUID()));
    }

    @Test
    public void test403isPassedToTheClient() {
        givenUserIsLoggedIn();
        mockServer.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.Forbidden.class,
                () -> appsController.update(UUID.randomUUID(), createAppDto()));
    }

    @Test
    public void test401isPassedToTheClient() {
        givenUserIsLoggedIn();
        mockServer.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.Unauthorized.class, () -> appsController.delete(UUID.randomUUID()));
    }

    @Test
    public void getAppsLayersUnauthenticated() throws IOException {
        //GIVEN
        givenUserIsNotAuthenticated();
        UUID appID = UUID.randomUUID();

        mockServer.expect(requestTo(String.format("/apps/%s?includeDefaultCollections=true", appID)))
                .andExpect(method(GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(
                        withSuccess(readFile(this, "layers-api/apps.defaultLayers.json"), MediaType.APPLICATION_JSON));

        //WHEN
        List<Layer> result = appsController.getListOfLayers(appID);

        //THEN
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getLegend());
    }

    @Test
    public void getAppsLayersAuthenticated() throws IOException {
        //GIVEN
        givenUserIsLoggedIn();
        UUID appID = UUID.randomUUID();

        mockServer.expect(requestTo(String.format("/apps/%s?includeDefaultCollections=true", appID)))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(
                        withSuccess(readFile(this, "layers-api/apps.defaultLayers.json"), MediaType.APPLICATION_JSON));

        //WHEN
        List<Layer> result = appsController.getListOfLayers(appID);

        //THEN
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getLegend());
    }

    @Test
    public void updateAppsLayers() throws IOException {
        //GIVEN
        givenUserIsLoggedIn();
        UUID appID = UUID.randomUUID();
        String randomJsonString = "{\"id\":\"623705fe3955c57d8b2c85ba\",\"index\":1}";

        mockServer.expect(requestTo(String.format("/apps/%s", appID)))
                .andExpect(method(PUT))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andExpect(request -> {
                    String s = request.getBody().toString();
                    assertThat(s, hasJsonPath("$.showAllPublicLayers", is(Boolean.FALSE)));
                    assertThat(s, hasJsonPath("$.isPublic", is(Boolean.TRUE)));
                    assertThat(s, hasJsonPath("$.layers", hasSize(1)));
                    assertThat(s, hasJsonPath("$.layers[0].layerId", is("testLayerId")));
                    assertThat(s, hasJsonPath("$.layers[0].isDefault", is(Boolean.TRUE)));
                    assertThat(s, hasJsonPath("$.layers[0].styleRule.id", is("623705fe3955c57d8b2c85ba")));
                    assertThat(s, hasJsonPath("$.layers[0].styleRule.index", is(1)));
                })
                .andRespond(
                        withSuccess(readFile(this, "layers-api/apps.defaultLayers.json"), MediaType.APPLICATION_JSON));

        //WHEN
        List<Layer> result = appsController.updateListOfLayers(appID, Collections.singletonList(
                new AppLayerUpdateDto(LAYER_PREFIX + "testLayerId", true,
                        (ObjectNode) new ObjectMapper().readTree(randomJsonString))
        ));

        //THEN
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getLegend());
    }

    private AppDto createAppDto() {
        AppDto dto = new AppDto();
        dto.setName("name");
        dto.setDescription("desc");
        dto.setPublic(true);
        dto.setFeatures(List.of("map_layers_panel"));
        dto.setCenterGeometry(new Point(new double[]{1d, 2d}));
        dto.setZoom(BigDecimal.valueOf(1.2345));
        return dto;
    }

}
