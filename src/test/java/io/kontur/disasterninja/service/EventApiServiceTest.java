package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.resource.exception.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class EventApiServiceTest {

    @Mock
    EventApiClient client;
    @Mock
    KeycloakAuthorizationService authorizationService;
    @InjectMocks
    EventApiService service;

    @Test
    public void testGetEvents_TokenRequest() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");
        //when
        service.getEvents();
        //then
        verify(authorizationService, times(1)).getAccessToken();
        verify(client, times(1)).getEvents("testToken");
    }

    @Test
    public void testGetEvents_EventSorting() {
        //given
        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto mostPopulatedEvent = factory.manufacturePojo(EventApiEventDto.class);
        mostPopulatedEvent.getEventDetails().put("population", 100);

        EventApiEventDto oldEvent = factory.manufacturePojo(EventApiEventDto.class);
        oldEvent.getEventDetails().put("population", 50);
        oldEvent.setUpdatedAt(OffsetDateTime.now().minusDays(2));

        EventApiEventDto moreRecentEvent = factory.manufacturePojo(EventApiEventDto.class);
        moreRecentEvent.getEventDetails().put("population", 50);
        moreRecentEvent.setUpdatedAt(OffsetDateTime.now());

        when(client.getEvents(any())).thenReturn(List.of(moreRecentEvent, oldEvent, mostPopulatedEvent));

        //when
        List<EventListDto> events = service.getEvents();

        //then
        assertEquals(mostPopulatedEvent.getEventId(), events.get(0).getEventId());
        assertEquals(moreRecentEvent.getEventId(), events.get(1).getEventId());
        assertEquals(oldEvent.getEventId(), events.get(2).getEventId());
    }


    @Test
    public void testGetEvent() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");

        PodamFactory factory = new PodamFactoryImpl();
        EventApiEventDto event = factory.manufacturePojo(EventApiEventDto.class);
        event.getEpisodes().get(0).setGeometries(new FeatureCollection(new Feature[0]));

        UUID eventId = UUID.randomUUID();
        when(client.getEvent(eventId, "testToken")).thenReturn(event);
        //when
        service.getEvent(eventId);
        //then
        verify(authorizationService, times(1)).getAccessToken();
        verify(client, times(1)).getEvent(eventId, "testToken");
    }

    @Test
    public void testGetEvent_notFound() {
        //given
        when(authorizationService.getAccessToken()).thenReturn("testToken");
        //when,then
        Assertions.assertThrows(WebApplicationException.class, () -> service.getEvent(UUID.randomUUID()));
    }

}