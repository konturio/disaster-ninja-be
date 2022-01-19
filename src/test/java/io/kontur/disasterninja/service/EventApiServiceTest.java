package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;
import static org.mockito.quality.Strictness.STRICT_STUBS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventApiServiceTest {

    @Mock
    EventApiClient client;
    @Mock
    KeycloakAuthorizationService authorizationService;
    @InjectMocks
    EventApiService service;

    private final String userToken = "some-user-token";
    private final String defaultAppFeed = "disaster-ninja-02";
    private final String defaultAppFeedDesc = "some desc";
    private final String userAppFeed = "some-user-feed";
    private final String userAppFeedDesc = "some user desc";

    @BeforeEach
    public void before() {
        String defaultAppToken = "some-app-token";
        when(authorizationService.getAccessToken()).thenReturn(defaultAppToken);
        when(client.getUserFeeds(defaultAppToken)).thenReturn(publicFeeds());
        when(client.getUserFeeds(userToken)).thenReturn(userFeeds());
    }

    @Test
    public void testPublicFeeds() {
        List<EventFeedDto> result = service.getUserFeeds(null);

        assertEquals(1, result.size());
        assertEquals(defaultAppFeed, result.get(0).getFeed());
        assertEquals(defaultAppFeedDesc, result.get(0).getDescription());
    }

    @Test
    public void testUserFeeds() {
        List<EventFeedDto> result = service.getUserFeeds(userToken);

        assertEquals(1, result.size());
        assertEquals(userAppFeed, result.get(0).getFeed());
        assertEquals(userAppFeedDesc, result.get(0).getDescription());
    }

    @Test
    public void testGetEvents_TokenRequest() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");
        //when
        service.getEvents(null, null);
        //then
        verify(authorizationService, times(1)).getAccessToken();
        verify(client, times(1)).getEvents("testToken", null);
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

        when(client.getEvents(any(), any())).thenReturn(List.of(moreRecentEvent, oldEvent, mostPopulatedEvent));

        //when
        List<EventListDto> events = service.getEvents(null, null);

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
    public void testGetEvent() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");

        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto event = factory.manufacturePojo(EventApiEventDto.class);
        event.getEpisodes().forEach(e -> e.setGeometries(new FeatureCollection(new Feature[0])));
        event.getEventDetails().put("populatedAreaKm2", "123234");

        UUID eventId = UUID.randomUUID();
        when(client.getEvent(eventId, "testToken", null)).thenReturn(event);
        //when
        EventDto result = service.getEvent(eventId, null, null);
        //then
        verify(authorizationService, times(1)).getAccessToken();
        verify(client, times(1)).getEvent(eventId, "testToken", null);
        //some EventDto fields
        assertEquals(event.getProperName(), result.getEventName());
        assertEquals(event.getLocation(), result.getLocation());
        assertEquals(123234L, result.getSettledArea());
        assertEquals(event.getUrls(), result.getExternalUrls());
    }

    @Test
    public void testGetEvent_notFound() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");
        //when,then
        Assertions.assertThrows(WebApplicationException.class, () -> service.getEvent(UUID.randomUUID(), null, null));
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