package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.*;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventApiServiceTest {

    private final String userToken = "some-user-token";
    private final String defaultAppToken = "some-app-token";
    private final String defaultAppFeed = "kontur-public";
    private final String defaultAppFeedDesc = "some desc";
    private final String userAppFeed = "some-user-feed";
    private final String userAppFeedDesc = "some user desc";

    @Mock
    private KeycloakAuthorizationService authorizationService;
    private EventApiClient client = mock(EventApiClient.class);
    private EventApiService service = new EventApiService(client, 1000);

    @BeforeEach
    public void before() {
        when(authorizationService.getAccessToken()).thenReturn(defaultAppToken);
    }

    public void givenJwtTokenIs(String tokenValue) {
        if (tokenValue == null) {
            tokenValue = defaultAppToken;
            when(client.getUserFeeds()).thenReturn(publicFeeds());
        } else {
            when(client.getUserFeeds()).thenReturn(userFeeds());

        }
        Authentication authentication = new JwtAuthenticationToken(Jwt.withTokenValue(tokenValue)
                .claim("some", "claim")
                .header(HttpHeaders.AUTHORIZATION, tokenValue)
                .build());
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testPublicFeeds() {
        givenJwtTokenIs(null);
        List<EventFeedDto> result = service.getUserFeeds();

        assertEquals(1, result.size());
        assertEquals(defaultAppFeed, result.get(0).getFeed());
        assertEquals(defaultAppFeedDesc, result.get(0).getDescription());
    }

    @Test
    public void testUserFeeds() {
        givenJwtTokenIs(userToken);
        List<EventFeedDto> result = service.getUserFeeds();

        assertEquals(1, result.size());
        assertEquals(userAppFeed, result.get(0).getFeed());
        assertEquals(userAppFeedDesc, result.get(0).getDescription());
    }

    @Test
    public void testGetEvents_EventSorting() {
        //given
        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto mostPopulatedEvent = factory.manufacturePojo(EventApiEventDto.class);
        mostPopulatedEvent.getEventDetails().put("population", 100);
        mostPopulatedEvent.getEventDetails().put("populatedAreaKm2", 10L);
        mostPopulatedEvent.setLocation("Some city");
        mostPopulatedEvent.setProperName("Proper name");
        mostPopulatedEvent.setUrls(List.of("http://google.com"));

        EventApiEventDto oldEvent = factory.manufacturePojo(EventApiEventDto.class);
        oldEvent.getEventDetails().put("population", 50);
        oldEvent.setUpdatedAt(OffsetDateTime.now().minusDays(2));

        EventApiEventDto moreRecentEvent = factory.manufacturePojo(EventApiEventDto.class);
        moreRecentEvent.getEventDetails().put("population", 50);
        moreRecentEvent.setUpdatedAt(OffsetDateTime.now());

        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        eventResponse.setData(List.of(moreRecentEvent, oldEvent, mostPopulatedEvent));
        eventResponse.setPageMetadata(new EventApiClient.PageMetadata());
        when(client.getEvents(eq("feed"), any(OffsetDateTime.class), isNull(), eq(1000), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(1000), eq(EventApiClient.SortOrder.DESC))).thenReturn(Optional.of(eventResponse));

        //when
        List<EventListDto> events = service.getEvents("feed", null);

        //then
        assertEquals(mostPopulatedEvent.getEventId(), events.get(0).getEventId());
        assertEquals(moreRecentEvent.getEventId(), events.get(1).getEventId());
        assertEquals(oldEvent.getEventId(), events.get(2).getEventId());

        //some EventListDto fields
        assertEquals(10d, events.get(0).getSettledArea());
        assertEquals("Some city", events.get(0).getLocation());
        assertEquals("Proper name", events.get(0).getEventName());
        assertEquals(1, events.get(0).getExternalUrls().size());
        assertEquals("http://google.com", events.get(0).getExternalUrls().get(0));
    }

    @Test
    public void testGetEvents_EmptyResults() {
        //given
        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        eventResponse.setData(Collections.emptyList());
        eventResponse.setPageMetadata(new EventApiClient.PageMetadata());

        when(client.getEvents(eq("feed"), any(OffsetDateTime.class), isNull(), eq(1000), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));

        EventApiClient.EventApiSearchEventResponse eventResponse2 = new EventApiClient.EventApiSearchEventResponse();
        eventResponse2.setPageMetadata(new EventApiClient.PageMetadata());
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(1000), eq(EventApiClient.SortOrder.DESC))).thenReturn(Optional.of(eventResponse2));

        //when
        List<EventListDto> events = service.getEvents("feed", null);

        //then
        assertEquals(0, events.size());
    }

    @Test
    public void testGetEvents_GetWithoutAfterFilter() {
        //given
        EventApiService localService = new EventApiService(client, 3);
        PodamFactory factory = new PodamFactoryImpl();

        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        eventResponse.setData(List.of(factory.manufacturePojo(EventApiEventDto.class), factory.manufacturePojo(EventApiEventDto.class)));
        EventApiClient.PageMetadata pageMetadata = new EventApiClient.PageMetadata();
        OffsetDateTime now = OffsetDateTime.now();
        pageMetadata.setNextAfterValue(now);
        eventResponse.setPageMetadata(pageMetadata);

        when(client.getEvents(eq("feed"), any(OffsetDateTime.class), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));

        EventApiClient.EventApiSearchEventResponse eventResponse2 = new EventApiClient.EventApiSearchEventResponse();
        eventResponse2.setData(List.of(factory.manufacturePojo(EventApiEventDto.class)));
        EventApiClient.PageMetadata pageMetadata2 = new EventApiClient.PageMetadata();
        eventResponse2.setPageMetadata(pageMetadata2);
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(3), eq(EventApiClient.SortOrder.DESC))).thenReturn(Optional.of(eventResponse2));
        when(client.getEvents(eq("feed"), eq(now), isNull(), eq(3), any())).thenThrow(new RuntimeException("wrong way"));

        //when
        List<EventListDto> events = localService.getEvents("feed", null);

        //then
        assertEquals(1, events.size());
    }

    @Test
    public void testGetEvents_Get4DayEventsInOneCall() {
        //given
        EventApiService localService = new EventApiService(client, 3);
        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto event1 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event2 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event3 = factory.manufacturePojo(EventApiEventDto.class);

        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        eventResponse.setData(List.of(event3, event2, event1));
        EventApiClient.PageMetadata pageMetadata = new EventApiClient.PageMetadata();
        OffsetDateTime now = OffsetDateTime.now();
        pageMetadata.setNextAfterValue(now);
        eventResponse.setPageMetadata(pageMetadata);

        when(client.getEvents(eq("feed"), any(OffsetDateTime.class), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));
        when(client.getEvents(eq("feed"), eq(now), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.empty());
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(3), any())).thenThrow(new RuntimeException("wrong way"));

        //when
        List<EventListDto> events = localService.getEvents("feed", null);

        //then
        assertEquals(3, events.size());
    }

    @Test
    public void testGetEvents_Get4DayEventsInTwoCalls() {
        //given
        EventApiService localService = new EventApiService(client, 3);
        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto event1 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event2 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event3 = factory.manufacturePojo(EventApiEventDto.class);

        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        eventResponse.setData(List.of(event3, event2, event1));
        EventApiClient.PageMetadata pageMetadata = new EventApiClient.PageMetadata();
        OffsetDateTime now = OffsetDateTime.now();
        pageMetadata.setNextAfterValue(now);
        eventResponse.setPageMetadata(pageMetadata);

        OffsetDateTime hourTrimmedDate = OffsetDateTime.now().minusDays(4).truncatedTo(ChronoUnit.HOURS);
        when(client.getEvents(eq("feed"), argThat(date -> date.isEqual(hourTrimmedDate)), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));

        EventApiClient.EventApiSearchEventResponse eventResponse2 = new EventApiClient.EventApiSearchEventResponse();
        eventResponse2.setData(List.of(factory.manufacturePojo(EventApiEventDto.class)));
        EventApiClient.PageMetadata pageMetadata2 = new EventApiClient.PageMetadata();
        eventResponse2.setPageMetadata(pageMetadata2);
        when(client.getEvents(eq("feed"), eq(now), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse2));
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(3), any())).thenThrow(new RuntimeException("wrong way"));

        //when
        List<EventListDto> events = localService.getEvents("feed", null);

        //then
        assertEquals(4, events.size());
    }

    @Test
    public void testGetEvents_Get4DayEventsInThreeCalls() {
        //given
        EventApiService localService = new EventApiService(client, 3);
        PodamFactory factory = new PodamFactoryImpl();

        EventApiClient.EventApiSearchEventResponse eventResponse = new EventApiClient.EventApiSearchEventResponse();
        EventApiEventDto event1 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event2 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event3 = factory.manufacturePojo(EventApiEventDto.class);
        eventResponse.setData(List.of(event3, event2, event1));
        EventApiClient.PageMetadata pageMetadata = new EventApiClient.PageMetadata();
        OffsetDateTime now = OffsetDateTime.now();
        pageMetadata.setNextAfterValue(now);
        eventResponse.setPageMetadata(pageMetadata);

        when(client.getEvents(eq("feed"), any(OffsetDateTime.class), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse));

        EventApiClient.EventApiSearchEventResponse eventResponse2 = new EventApiClient.EventApiSearchEventResponse();
        EventApiEventDto event4 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event5 = factory.manufacturePojo(EventApiEventDto.class);
        EventApiEventDto event6 = factory.manufacturePojo(EventApiEventDto.class);
        eventResponse2.setData(List.of(event4, event5, event6));
        EventApiClient.PageMetadata pageMetadata2 = new EventApiClient.PageMetadata();
        OffsetDateTime now2 = OffsetDateTime.now();
        pageMetadata2.setNextAfterValue(now2);
        eventResponse2.setPageMetadata(pageMetadata2);
        when(client.getEvents(eq("feed"), eq(now), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse2));

        EventApiClient.EventApiSearchEventResponse eventResponse3 = new EventApiClient.EventApiSearchEventResponse();
        eventResponse3.setData(List.of(factory.manufacturePojo(EventApiEventDto.class)));
        EventApiClient.PageMetadata pageMetadata3 = new EventApiClient.PageMetadata();
        eventResponse3.setPageMetadata(pageMetadata3);
        when(client.getEvents(eq("feed"), eq(now2), isNull(), eq(3), eq(EventApiClient.SortOrder.ASC))).thenReturn(Optional.of(eventResponse3));
        when(client.getEvents(eq("feed"), isNull(), isNull(), eq(3), any())).thenThrow(new RuntimeException("wrong way"));

        //when
        List<EventListDto> events = localService.getEvents("feed", null);

        //then
        assertEquals(7, events.size());
    }

    @Test
    public void testGetEvent() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");

        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto event = factory.manufacturePojo(EventApiEventDto.class);
        event.setGeometries(new FeatureCollection(new Feature[0]));
        event.getEpisodes().forEach(e -> e.setGeometries(new FeatureCollection(new Feature[0])));
        event.getEventDetails().put("populatedAreaKm2", "123234");

        UUID eventId = UUID.randomUUID();
        when(client.getEvent(eventId, null, false)).thenReturn(event);
        //when
        EventDto result = service.getEvent(eventId, null);
        //then
        verify(client, times(1)).getEvent(eventId, null, false);
        //some EventDto fields
        assertEquals(event.getProperName(), result.getEventName());
        assertEquals(event.getLocation(), result.getLocation());
        assertEquals(123234L, result.getSettledArea());
        assertEquals(event.getUrls(), result.getExternalUrls());
    }

    @Test
    public void testGetEventEpisodes() throws IOException {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");

        EventApiEventDto event = JsonUtil.readJson(
                readFile(this, "EventApiServiceTest.testGetEventEpisodes.json"), EventApiEventDto.class);


        UUID eventId = UUID.fromString("f4919979-3668-48f1-91af-94b3c5bffe2a");
        when(client.getEvent(eventId, null, true)).thenReturn(event);
        //when
        List<EventEpisodeListDto> result = service.getEventEpisodes(eventId, null);
        //then
        verify(client, times(1)).getEvent(eventId, null, true);
        //some EventDto fields
        assertEquals(5, result.size());
        assertEquals("Thermal anomaly in United States, California, San Diego County. Burnt area 2.518 km²",
                result.get(0).getName());
        assertEquals(Severity.MINOR, result.get(0).getSeverity());
        assertEquals("United States, California, San Diego County", result.get(0).getLocation());
        assertEquals("test.test", result.get(0).getExternalUrls().get(0));
        assertEquals(1661981220, result.get(0).getStartedAt().toEpochSecond());
        assertEquals(1662008460, result.get(0).getEndedAt().toEpochSecond());
        assertEquals(1661993950, result.get(0).getUpdatedAt().toEpochSecond());
        assertEquals("Polygon", result.get(0).getGeojson().getFeatures()[0].getGeometry().getType());
        assertTrue(result.get(0).getUpdatedAt().isBefore(result.get(1).getUpdatedAt()));
    }

    @Test
    public void testGetEvent_notFound() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");
        //when,then
        Assertions.assertThrows(WebApplicationException.class, () -> service.getEvent(UUID.randomUUID(), null));
    }

    private List<EventFeedDto> publicFeeds() {
        EventFeedDto eventFeedDto = new EventFeedDto(defaultAppFeed, defaultAppFeedDesc);
        return List.of(eventFeedDto);
    }

    private List<EventFeedDto> userFeeds() {
        EventFeedDto eventFeedDto = new EventFeedDto(userAppFeed, userAppFeedDesc);
        return List.of(eventFeedDto);
    }

}