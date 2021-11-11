package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.service.converter.EventDtoConverter;
import io.kontur.disasterninja.service.converter.EventListEventDtoConverter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.kontur.disasterninja.dto.EventType.CYCLONE;
import static io.kontur.disasterninja.dto.EventType.OTHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventApiConvertersTest {
    private static EventApiEventDto testEvent() {
        EventApiEventDto event = new EventApiEventDto();
        event.setProperName("proper name");
        event.setLocation("location 1");
        event.setUrls(null);
        event.setEventDetails(null);

        FeedEpisode episode = new FeedEpisode();
        episode.setType(EventType.OTHER.toString());

        List<FeedEpisode> episodes = new ArrayList<>();
        episodes.add(episode);

        event.setEpisodes(episodes);
        return event;
    }

    @Test
    public void eventDtoTestNulls() {
        EventApiEventDto event = testEvent();
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(0L, dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventDtoTestEmptyCollections() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.setUrls(new ArrayList<>());
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(0L, dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventDtoTest() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.getEventDetails().put("populatedAreaKm2", 100.12);
        event.setUrls(new ArrayList<>());
        event.getUrls().add("http://google.com");
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }

    @Test
    public void eventDtoTestNames() {
        EventApiEventDto event = testEvent();

        //1. type = other & properName != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(OTHER.toString());
        EventDto dto = EventDtoConverter.convert(event);
        assertEquals("some name", dto.getEventName());

        //2. type != other & propername != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(CYCLONE.toString());
        dto = EventDtoConverter.convert(event);
        assertEquals(CYCLONE.getName() + " some name", dto.getEventName());

        //3. propername == null
        event.setProperName(null);
        dto = EventDtoConverter.convert(event);
        assertEquals(CYCLONE.getName(), dto.getEventName());
    }

    @Test
    public void eventListDtoTestNulls() {
        EventApiEventDto event = testEvent();
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(0L, dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventListDtoTestEmptyCollections() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.setUrls(new ArrayList<>());
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(0L, dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventListDtoTest() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.getEventDetails().put("populatedAreaKm2", 100.12);
        event.setUrls(new ArrayList<>());
        event.getUrls().add("http://google.com");
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocations());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }


    @Test
    public void eventListDtoTestNames() {
        EventApiEventDto event = testEvent();

        //1. type = other & properName != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(OTHER.toString());
        EventListDto dto = EventListEventDtoConverter.convert(event);
        assertEquals("some name", dto.getEventName());

        //2. type != other & propername != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(CYCLONE.toString());
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(CYCLONE.getName() + " some name", dto.getEventName());

        //3. propername == null
        event.setProperName(null);
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(CYCLONE.getName(), dto.getEventName());
    }
}
