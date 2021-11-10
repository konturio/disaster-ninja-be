package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(EventApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class EventApiClientTest {

    @Autowired
    private EventApiClient client;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void testGetEvents() throws IOException {
        //given
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                matchesRegex(Pattern.compile(
                    "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&after=\\d{4}-\\d{2}-\\d{2}[tT]\\d{2}:\\d{2}Z&episodeFilterType=LATEST&limit=1000&sortOrder=ASC"))))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer JwtTestToken"))
            .andRespond(r -> {
                if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                    return withNoContent().createResponse(r);
                }
                return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.response.json"),
                    MediaType.APPLICATION_JSON).createResponse(r);
            });

        //when
        List<EventApiEventDto> events = client.getEvents("JwtTestToken");

        //then
        assertEquals(2, events.size());
    }

    @Test
    public void testGetEvent() throws IOException {
        //given
        server.expect(ExpectedCount.once(), requestTo("/v1/event?feed=testFeedName&eventId=1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer JwtTestToken"))
                .andRespond(withSuccess(readFile(this, "EventApiClientTest.testGetEvent.response.json"),
                        MediaType.APPLICATION_JSON));
        //when
        EventApiEventDto event = client.getEvent(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"),
                "JwtTestToken");

        //then
        assertNotNull(event);
        assertEquals(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"), event.getEventId());
    }
}