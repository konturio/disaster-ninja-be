package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.dto.eventapi.EventDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.time.OffsetDateTime;
import java.util.List;

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
        EventDto mostPopulatedEvent = factory.manufacturePojo(EventDto.class);
        mostPopulatedEvent.getEventDetails().put("population", 100);

        EventDto oldEvent = factory.manufacturePojo(EventDto.class);
        oldEvent.getEventDetails().put("population", 50);
        oldEvent.setUpdatedAt(OffsetDateTime.now().minusDays(2));

        EventDto moreRecentEvent = factory.manufacturePojo(EventDto.class);
        moreRecentEvent.getEventDetails().put("population", 50);
        moreRecentEvent.setUpdatedAt(OffsetDateTime.now());

        when(client.getEvents(any())).thenReturn(List.of(moreRecentEvent, oldEvent, mostPopulatedEvent));

        //when
        List<EventListEventDto> events = service.getEvents();

        //then
        assertEquals(mostPopulatedEvent.getEventId(), events.get(0).getEventId());
        assertEquals(moreRecentEvent.getEventId(), events.get(1).getEventId());
        assertEquals(oldEvent.getEventId(), events.get(2).getEventId());
    }
}