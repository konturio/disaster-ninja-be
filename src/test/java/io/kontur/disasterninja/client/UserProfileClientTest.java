package io.kontur.disasterninja.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UserProfileClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class UserProfileClientTest extends TestDependingOnUserAuth {

    private final String DEFAULT_FEED = "kontur-public";
    private final String USER_FEED = "some-user-feed";
    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private UserProfileClient client;

    @Test
    public void getUserFeedTest() {
        //given
        givenJwtTokenIs(jwt);

        server.expect(ExpectedCount.times(1), requestTo("/features/user_feed"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + jwt))
            .andRespond(r -> withSuccess(USER_FEED, MediaType.APPLICATION_JSON).createResponse(r));

        //when
        String feed = client.getUserDefaultFeed();

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertEquals(USER_FEED, feed);
    }

    @Test
    public void getUserFeedUnauthenticatedTest() {
        //given
        givenUserIsNotAuthenticated();

        server.expect(ExpectedCount.times(1), requestTo("/features/user_feed"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(headerDoesNotExist("Authorization"))
            .andRespond(r -> withSuccess(DEFAULT_FEED, MediaType.APPLICATION_JSON).createResponse(r));

        //when
        String feed = client.getUserDefaultFeed();

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertEquals(DEFAULT_FEED, feed);
    }


}