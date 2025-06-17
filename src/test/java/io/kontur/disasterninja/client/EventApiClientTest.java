package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.GeometryFilterType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(EventApiClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class EventApiClientTest extends TestDependingOnUserAuth {

    @Autowired
    private EventApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    public void testGetEventsWithoutBbox() {
        //given
        givenJwtTokenIs(getUserToken());
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&limit=1000" +
                                        "&episodeFilterType=NONE&sortOrder=ASC&geometryFilterType=ALL" +
                                        "&after=\\d{4}-\\d{2}-\\d{2}[tT]\\d{2}:\\d{2}:\\d{2}.\\d+Z"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(r -> {
                    if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                        return withNoContent().createResponse(r);
                    }
                    return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.withoutEpisodes.response.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                });

        //when
        Optional<EventApiClient.EventApiSearchEventResponse> events = client.getEvents("testFeedName", OffsetDateTime.now(), emptyList(), 1000, EventApiClient.SortOrder.ASC);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertTrue(events.isPresent());
        assertEquals(2, events.get().getData().size());
    }

    @Test
    public void testGetEventsTruncateAfterToHours() {
        //given
        givenJwtTokenIs(getUserToken());
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&limit=1000" +
                                        "&episodeFilterType=NONE&sortOrder=ASC&geometryFilterType=ALL" +
                                        "&after=\\d{4}-\\d{2}-\\d{2}[tT]\\d{2}:00:00Z"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(r -> {
                    if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                        return withNoContent().createResponse(r);
                    }
                    return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.withoutEpisodes.response.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                });

        //when
        Optional<EventApiClient.EventApiSearchEventResponse> events = client.getEvents("testFeedName", OffsetDateTime.now().truncatedTo(
                ChronoUnit.HOURS), emptyList(), 1000, EventApiClient.SortOrder.ASC);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertTrue(events.isPresent());
        assertEquals(2, events.get().getData().size());
    }

    @Test
    public void testGetEventsWithBbox() {
        //given
        givenJwtTokenIs(getUserToken());
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&limit=1000" +
                                        "&episodeFilterType=NONE&sortOrder=DESC&geometryFilterType=ALL" +
                                        "&after=\\d{4}-\\d{2}-\\d{2}[tT]\\d{2}:\\d{2}:\\d{2}.\\d+Z" +
                                        "&bbox=1.1&bbox=2.2&bbox=3.3&bbox=4.4"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(r -> {
                    if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                        return withNoContent().createResponse(r);
                    }
                    return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.withoutEpisodes.response.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                });

        //when
        Optional<EventApiClient.EventApiSearchEventResponse> events = client.getEvents("testFeedName", OffsetDateTime.now(),
                Arrays.asList(new BigDecimal("1.1"), new BigDecimal("2.2"), new BigDecimal("3.3"), new BigDecimal("4.4")), 1000, EventApiClient.SortOrder.DESC);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertTrue(events.isPresent());
        assertEquals(2, events.get().getData().size());
    }

    @Test
    public void testGetEventsWithoutAfter() {
        //given
        givenJwtTokenIs(getUserToken());
        server.expect(ExpectedCount.times(2), r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&limit=1000" +
                                        "&episodeFilterType=NONE&sortOrder=ASC&geometryFilterType=ALL" +
                                        "&bbox=1.1&bbox=2.2&bbox=3.3&bbox=4.4"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(r -> {
                    if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                        return withNoContent().createResponse(r);
                    }
                    return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.withoutEpisodes.response.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                });

        //when
        Optional<EventApiClient.EventApiSearchEventResponse> events = client.getEvents("testFeedName", null,
                Arrays.asList(new BigDecimal("1.1"), new BigDecimal("2.2"), new BigDecimal("3.3"), new BigDecimal("4.4")), 1000, null);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertTrue(events.isPresent());
        assertEquals(2, events.get().getData().size());
    }

    @Test
    public void testGetLatestEvents() {
        //given
        givenJwtTokenIs(getUserToken());
        server.expect(ExpectedCount.times(1), r -> assertThat(r.getURI().toString(),
                        matchesRegex(Pattern.compile(
                                "/v1/\\?feed=testFeedName&severities=EXTREME,SEVERE,MODERATE&limit=1000" +
                                        "&sortOrder=DESC&episodeFilterType=LATEST&types=type1&types=type2"))))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken()))
                .andRespond(r -> {
                    if (r.getURI().toString().contains("2021-09-28T14:46")) { //last page
                        return withNoContent().createResponse(r);
                    }
                    return withSuccess(readFile(this, "EventApiClientTest.testGetEvents.withoutEpisodes.response.json"),
                            MediaType.APPLICATION_JSON).createResponse(r);
                });

        //when
        client.getLatestEvents(List.of("type1", "type2"), "testFeedName",1000);

        //then
        verify(securityContext, times(1)).getAuthentication();
    }

    @Test
    public void testGetEvent() throws IOException {
        //given
        givenJwtTokenIs("JwtTestToken");
        server.expect(ExpectedCount.once(),
                        requestTo("/v1/event?feed=testFeedName&eventId=1ec05e2b-7d18-490c-ac9f-c33609fdc7a7&episodeFilterType=ANY&geometryFilterType=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + getUserToken()))
                .andRespond(withSuccess(readFile(this, "EventApiClientTest.testGetEvent.response.json"),
                        MediaType.APPLICATION_JSON));
        //when
        EventApiEventDto event = client.getEvent(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"),
                "testFeedName", true, GeometryFilterType.ALL);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertNotNull(event);
        assertEquals(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"), event.getEventId());
    }

    @Test
    public void testGetEventWithoutEpisodes() throws IOException {
        //given
        givenJwtTokenIs("JwtTestToken");
        server.expect(ExpectedCount.once(),
                        requestTo("/v1/event?feed=testFeedName&eventId=1ec05e2b-7d18-490c-ac9f-c33609fdc7a7&episodeFilterType=NONE&geometryFilterType=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + getUserToken()))
                .andRespond(withSuccess(readFile(this, "EventApiClientTest.testGetEvent.response.json"),
                        MediaType.APPLICATION_JSON));
        //when
        EventApiEventDto event = client.getEvent(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"),
                "testFeedName", false, GeometryFilterType.ALL);

        //then
        verify(securityContext, times(1)).getAuthentication();
        assertNotNull(event);
        assertEquals(UUID.fromString("1ec05e2b-7d18-490c-ac9f-c33609fdc7a7"), event.getEventId());
    }
}