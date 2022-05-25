package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.FeatureDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.kontur.disasterninja.controller.FeaturesController.PATH;
import static io.kontur.disasterninja.dto.FeatureDto.FeatureType.UI_PANEL;
import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserProfileClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class FeatureControllerTest extends TestDependingOnUserAuth {

    @Autowired
    private MockRestServiceServer userProfileApi;
    @Autowired
    private UserProfileClient userProfileClient;
    private FeaturesController featuresController;

    @BeforeEach
    public void before() {
        featuresController = new FeaturesController(userProfileClient);
    }

    @Test
    public void getFeaturesWithNoAppId() throws IOException {
        //given
        givenUserIsNotAuthenticated();
        userProfileApi.expect(ExpectedCount.once(), requestTo(PATH))
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
        userProfileApi.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
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
        userProfileApi.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
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
        userProfileApi.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
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
        userProfileApi.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
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
        userProfileApi.expect(ExpectedCount.once(), requestToUriTemplate(PATH + "?appId={appId}", appId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNAUTHORIZED));

        //when-then
        assertThrows(HttpClientErrorException.Unauthorized.class, () -> featuresController.getUserAppFeatures(appId));
    }

}
