package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.eventapi.EventDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(EventApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class EventApiClientTest {

    @Autowired
    private EventApiClient client;
    @Autowired
    @Qualifier("eventApiRestTemplate")
    private MockRestServiceServer server;

    @Test
    public void testGetEvents() throws IOException {
        //given
        String then = LocalDate.now()
                .minusDays(4)
                .toString();

        server.expect(r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&after=" + then + "(t|T)\\d{2}:\\d{2}(:\\d{2})?Z&episodeFilterType=LATEST&limit=1000&sortOrder=ASC"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer JwtTestToken"))
                .andRespond(withSuccess(readFile(this, "EventApiClientTest.testGetEvents.response.json"),
                        MediaType.APPLICATION_JSON));

        //when
        List<EventDto> events = client.getEvents("JwtTestToken");

        //then
        assertEquals(2, events.size());
    }
}