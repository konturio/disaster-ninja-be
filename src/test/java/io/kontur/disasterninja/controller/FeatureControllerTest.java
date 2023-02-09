package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.FeatureDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.kontur.disasterninja.service.LiveSensorFeatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.*;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpClientErrorException;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.controller.FeaturesController.PATH;
import static io.kontur.disasterninja.dto.FeatureDto.FeatureType.UI_PANEL;
import static io.kontur.disasterninja.util.JsonUtil.readJson;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = {UserProfileClient.class, LiveSensorFeatureService.class, LayersApiClient.class})
@AutoConfigureWebClient(registerRestTemplate = true)
public class FeatureControllerTest extends TestDependingOnUserAuth {

    private final String defaultAppToken = "some-app-token";

    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private UserProfileClient userProfileClient;
    @Autowired
    private LiveSensorFeatureService liveSensorFeatureService;
    @SpyBean
    private KeycloakAuthorizationService authorizationService;

    private FeaturesController featuresController;

    @BeforeEach
    public void before() {
        featuresController = new FeaturesController(userProfileClient, liveSensorFeatureService);
        when(authorizationService.getAccessToken()).thenReturn(defaultAppToken);
        mockServer.reset();
    }

    @Test
    public void getFeaturesWithNoAppId() throws IOException {
        //given
        givenUserIsNotAuthenticated();
        mockServer.expect(ExpectedCount.once(), requestTo(PATH))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess(readFile(this, "ups/dn2Features.json"),
                        MediaType.APPLICATION_JSON));

        //when
        List<FeatureDto> response = featuresController.getUserAppFeatures(null);

        //then
        assertEquals(28, response.size());
        assertTrue(response.contains(
                new FeatureDto("episode_list", "Episode list", UI_PANEL))); //just random feature
    }

    @Test
    public void getFeaturesWithSomeAppId() throws IOException {
        //given
        UUID appId = UUID.randomUUID();
        givenUserIsNotAuthenticated();
        mockServer.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess(readFile(this, "ups/someAppFeatures.json"),
                        MediaType.APPLICATION_JSON));

        //when
        List<FeatureDto> response = featuresController.getUserAppFeatures(appId);

        //then
        assertEquals(2, response.size());
        assertTrue(response.contains(new FeatureDto("analytics_panel", "Analytics panel", UI_PANEL)));
        assertTrue(response.contains(new FeatureDto("events_list", "Events list", UI_PANEL)));
    }

    @Test
    public void userTokenIsPassedToUpsRequest() throws IOException {
        givenUserIsLoggedIn();
        UUID appId = UUID.randomUUID();
        //given
        mockServer.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(withSuccess(readFile(this, "ups/someAppFeatures.json"),
                        MediaType.APPLICATION_JSON));

        //when
        List<FeatureDto> response = featuresController.getUserAppFeatures(appId);

        //then
        assertEquals(2, response.size());
    }

    @Test
    public void test404isPassedToTheClient() {
        givenUserIsLoggedIn();
        UUID appId = UUID.randomUUID();
        //given
        mockServer.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

        //when-then
        assertThrows(HttpClientErrorException.NotFound.class, () -> featuresController.getUserAppFeatures(appId));
    }

    @Test
    public void test403isPassedToTheClient() {
        givenUserIsLoggedIn();
        UUID appId = UUID.randomUUID();
        //given
        mockServer.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN));

        //when-then
        assertThrows(HttpClientErrorException.Forbidden.class, () -> featuresController.getUserAppFeatures(appId));
    }

    @Test
    public void test401isPassedToTheClient() {
        givenUserIsNotAuthenticated();
        UUID appId = UUID.randomUUID();
        //given
        mockServer.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNAUTHORIZED));

        //when-then
        assertThrows(HttpClientErrorException.Unauthorized.class, () -> featuresController.getUserAppFeatures(appId));
    }

    @Test
    public void appendLiveSensorData() throws IOException {
        givenUserIsLoggedIn();
        //given
        String geoJson = readFile(this, "layers-api/items.live-sensor.json");

        mockServer.expect(ExpectedCount.once(), requestToUriTemplate("/collections/live-sensor/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(geoJson))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + defaultAppToken))
                .andRespond(withSuccess(geoJson, MediaType.APPLICATION_JSON));

        //when
        ResponseEntity<?> response = featuresController.liveSensor(readJson(geoJson, FeatureCollection.class));

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    public void appendEmptyLiveSensorData() {
        givenUserIsLoggedIn();
        //given
        mockServer.expect(ExpectedCount.never(), requestToUriTemplate("/collections/live-sensor/items"));

        //when
        ResponseEntity<?> response = featuresController.liveSensor(new FeatureCollection(new Feature[0]));

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        mockServer.verify();
    }

}
