package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
    private final String defaultAppToken = "some-app-token";
    private final String defaultAppFeed = "kontur-public";
    private final String defaultAppFeedDesc = "some desc";
    private final String userAppFeed = "some-user-feed";
    private final String userAppFeedDesc = "some user desc";

    @BeforeEach
    public void before() {
        when(authorizationService.getAccessToken()).thenReturn(defaultAppToken);
    }

    public void givenJwtTokenIs(String jwt) {
        if (jwt == null) {
            jwt = defaultAppToken;
            when(client.getUserFeeds()).thenReturn(publicFeeds());
        } else {
            when(client.getUserFeeds()).thenReturn(userFeeds());

        }
        Authentication authentication = new BearerTokenAuthenticationToken(jwt);
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

        when(client.getEvents(any())).thenReturn(List.of(moreRecentEvent, oldEvent, mostPopulatedEvent));

        //when
        List<EventListDto> events = service.getEvents(null);

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
        when(client.getEvent(eventId, null)).thenReturn(event);
        //when
        EventDto result = service.getEvent(eventId, null);
        //then
        verify(client, times(1)).getEvent(eventId, null);
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