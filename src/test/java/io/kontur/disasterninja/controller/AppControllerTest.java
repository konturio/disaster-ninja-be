package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.AppDto;
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
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.controller.AppsController.PATH;
import static io.kontur.disasterninja.service.GeometryTransformer.geometriesAreEqual;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserProfileClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class AppControllerTest extends TestDependingOnUserAuth {
    private static final UUID DN2_ID = UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb54");
    @Autowired
    private MockRestServiceServer userProfileApi;
    @Autowired
    private UserProfileClient userProfileClient;
    private AppsController appsController;

    @BeforeEach
    public void before() {
        appsController = new AppsController(userProfileClient);
    }

    @Test
    public void getAppsListUnauthenticated() throws IOException {
        givenUserIsNotAuthenticated();
        userProfileApi.expect(requestTo(PATH))
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
        userProfileApi.expect(requestTo(PATH))
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
    public void getAppUnauthenticated() throws IOException {
        givenUserIsNotAuthenticated();
        userProfileApi.expect(requestTo(PATH + "/" + DN2_ID))
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
        assertTrue(result.isPublic());
    }

    @Test
    public void getApp() throws IOException {
        givenUserIsLoggedIn();
        UUID appId = UUID.fromString("58851b50-9574-4aec-a3a6-425fa18dcb11");
        userProfileApi.expect(requestTo(PATH + "/" + appId))
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
    }

    @Test
    public void deleteApp() {
        givenUserIsLoggedIn();
        UUID appId = UUID.randomUUID();
        userProfileApi.expect(requestTo(PATH + "/" + appId))
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

        userProfileApi.expect(requestTo(PATH + "/" + appId))
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

        userProfileApi.expect(requestTo(PATH))
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

        assertEquals(appId, result.getId());
        assertEquals(true, result.getOwnedByUser());
    }


    @Test
    public void test404isPassedToTheClient() {
        givenUserIsLoggedIn();
        userProfileApi.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.NotFound.class, () -> appsController.get(UUID.randomUUID()));
    }

    @Test
    public void test403isPassedToTheClient() {
        givenUserIsLoggedIn();
        userProfileApi.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.Forbidden.class, () -> appsController.update(UUID.randomUUID(), createAppDto()));
    }

    @Test
    public void test401isPassedToTheClient() {
        givenUserIsLoggedIn();
        userProfileApi.expect(anything()).andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.Unauthorized.class, () -> appsController.delete(UUID.randomUUID()));
    }

    private AppDto createAppDto() {
        AppDto dto = new AppDto();
        dto.setName("name");
        dto.setDescription("desc");
        dto.setPublic(true);
        dto.setFeatures(List.of("map_layers_panel"));
        dto.setCenterGeometry(new Point(new double[]{1d, 2d}));
        return dto;
    }

}
