package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(EventApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class EventApiClientTest {

    private final String jwt = "JwtTestToken";
    @Autowired
    private EventApiClient client;
    @Autowired
    private MockRestServiceServer server;
    @Mock
    SecurityContext securityContext;
    @MockBean
    KeycloakAuthorizationService authorizationService;

    public void givenJwtTokenIs(String jwt) {
        Authentication authentication = new BearerTokenAuthenticationToken(jwt);
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetEvents() {
        //given
        givenJwtTokenIs(jwt);
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                matchesRegex(Pattern.compile(
                    "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&after=\\d{4}-\\d{2}-\\d{2}[tT]" +
                        "\\d{2}:\\d{2}:\\d{2}.?\\d{0,3}Z&episodeFilterType=LATEST&limit=1000&sortOrder=ASC"))))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + jwt))
            .andRespond(r -> {
                if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                    return withNoContent().createResponse(r);
                }
                return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.response.json"),
                    MediaType.APPLICATION_JSON).createResponse(r);
            });

        //when
        List<EventApiEventDto> events = client.getEvents(null);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertEquals(2, events.size());
    }

    @Test
    public void testGetEvent() throws IOException {
        //given
        givenJwtTokenIs("JwtTestToken");
        server.expect(ExpectedCount.once(), requestTo("/v1/event?feed=testFeedName&eventId=1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + jwt))
                .andRespond(withSuccess(readFile(this, "EventApiClientTest.testGetEvent.response.json"),
                        MediaType.APPLICATION_JSON));
        //when
        EventApiEventDto event = client.getEvent(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"), null);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertNotNull(event);
        assertEquals(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"), event.getEventId());
    }
}