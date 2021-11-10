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
        assertEquals(event.getLocation(), dto.getLocation());
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
        assertEquals(event.getLocation(), dto.getLocation());
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
        assertEquals(event.getLocation(), dto.getLocation());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }

    @Test
    public void eventListDtoTestNulls() {
        EventApiEventDto event = testEvent();
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocation());
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
        assertEquals(event.getLocation(), dto.getLocation());
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
        assertEquals(event.getLocation(), dto.getLocation());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }
}
