package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.Severity;
import io.kontur.disasterninja.dto.EventEpisodeListDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.service.converter.EventDtoConverter;
import io.kontur.disasterninja.service.converter.EventListEventDtoConverter;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.kontur.disasterninja.dto.EventType.*;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.*;

public class EventApiConvertersTest {

    private static EventApiEventDto testEvent() {
        EventApiEventDto event = new EventApiEventDto();
        event.setProperName("proper name");
        event.setDescription("event description");
        event.setLocation("location 1");
        event.setUrls(null);
        event.setEventDetails(null);
        event.setType(EventType.FLOOD.toString());
        event.setSeverity(Severity.MODERATE);
        event.setUpdatedAt(OffsetDateTime.parse("2007-12-03T10:15:30+01:00", ISO_OFFSET_DATE_TIME));

        FeedEpisode episode = new FeedEpisode();
        episode.setType(EventType.OTHER.toString());

        List<FeedEpisode> episodes = new ArrayList<>();
        episodes.add(episode);

        event.setEpisodes(episodes);
        event.setEpisodeCount(1);
        return event;
    }

    @Test
    public void eventDtoTestNulls() {
        EventApiEventDto event = testEvent();
        event.setType(OTHER.toString());
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getLocation(), dto.getLocation());
        assertTrue(dto.getExternalUrls().isEmpty());

        assertNull(dto.getSettledArea());
        assertNull(dto.getOsmGaps());
        assertNull(dto.getAffectedPopulation());
        assertNull(dto.getLoss());
    }

    @Test
    public void eventDtoTestEmptyCollections() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.setUrls(new ArrayList<>());
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getLocation(), dto.getLocation());
        assertNull(dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventDtoTest() {
        EventApiEventDto event = testEvent();
        event.setType(OTHER.toString());
        event.setEventDetails(new HashMap<>());
        event.getEventDetails().put("populatedAreaKm2", 100.12);
        event.getEventDetails().put("osmGapsPercentage", 30);
        event.getEventDetails().put("population", 50);
        event.getEventDetails().put("loss", 531.14);
        event.setUrls(new ArrayList<>());
        event.getUrls().add("http://google.com");
        EventDto dto = EventDtoConverter.convert(event);

        assertEquals(event.getProperName(), dto.getEventName());
        assertEquals(event.getDescription(), dto.getDescription());
        assertEquals(event.getLocation(), dto.getLocation());
        assertEquals(OffsetDateTime.parse("2007-12-03T10:15:30+01:00", ISO_OFFSET_DATE_TIME), dto.getUpdatedAt());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(30, dto.getOsmGaps());
        assertEquals(50, dto.getAffectedPopulation());
        assertEquals(531, dto.getLoss());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }

    @Test
    public void eventDtoTestNames() {
        EventApiEventDto event = testEvent();
        event.setType(OTHER.toString());
        //1. type = other & properName != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(OTHER.toString());
        EventDto dto = EventDtoConverter.convert(event);
        assertEquals("some name", dto.getEventName());

        //2. type != other & propername != null
        event.setProperName("some name");
        event.setType(DROUGHT.toString());
        event.getEpisodes().get(0).setType(CYCLONE.toString());
        dto = EventDtoConverter.convert(event);
        assertEquals(DROUGHT.getName() + " some name", dto.getEventName());

        //3. propername == null
        event.setProperName(null);
        dto = EventDtoConverter.convert(event);
        assertEquals(DROUGHT.getName(), dto.getEventName());
    }

    @Test
    public void eventDtoTestEpisodeCount() {
        EventApiEventDto event = testEvent();
        EventDto dto = EventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 1);

        //1. with 0 episodes
        event.setEpisodeCount(0);
        dto = EventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 0);
        
        //2. with 2 episodes
        event.setEpisodeCount(2);
        dto = EventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 2);
    }

    @Test
    public void eventDtoSeverityDataTest() {
        EventApiEventDto event = testEvent();
        event.setSeverityData(new HashMap<>());
        event.getSeverityData().put("magnitude", 5.6);
        event.getSeverityData().put("categorySaffirSimpson", 3);

        EventDto dto = EventDtoConverter.convert(event);
        assertEquals(5.6, dto.getMagnitude());
        assertEquals(3, dto.getCategory());

        FeedEpisode episode = event.getEpisodes().get(0);
        episode.setSeverityData(new HashMap<>());
        episode.getSeverityData().put("magnitude", 4.5);
        episode.getSeverityData().put("categorySaffirSimpson", 2);

        EventEpisodeListDto episodeDto = EventDtoConverter.convertEventEpisode(episode);
        assertEquals(4.5, episodeDto.getMagnitude());
        assertEquals(2, episodeDto.getCategory());
    }

    @Test
    public void eventDtoSeverityDataEdgeCasesTest() {
        EventApiEventDto event = testEvent();

        EventDto dto = EventDtoConverter.convert(event);
        assertNull(dto.getMagnitude());
        assertNull(dto.getCategory());

        FeedEpisode episode = event.getEpisodes().get(0);
        EventEpisodeListDto episodeDto = EventDtoConverter.convertEventEpisode(episode);
        assertNull(episodeDto.getMagnitude());
        assertNull(episodeDto.getCategory());

        event.setSeverityData(new HashMap<>());
        dto = EventDtoConverter.convert(event);
        assertNull(dto.getMagnitude());
        assertNull(dto.getCategory());

        episode.setSeverityData(new HashMap<>());
        episodeDto = EventDtoConverter.convertEventEpisode(episode);
        assertNull(episodeDto.getMagnitude());
        assertNull(episodeDto.getCategory());

        event.getSeverityData().put("magnitude", null);
        event.getSeverityData().put("categorySaffirSimpson", null);
        dto = EventDtoConverter.convert(event);
        assertEquals(0.0, dto.getMagnitude());
        assertEquals(0, dto.getCategory());

        episode.getSeverityData().put("magnitude", null);
        episode.getSeverityData().put("categorySaffirSimpson", null);
        episodeDto = EventDtoConverter.convertEventEpisode(episode);
        assertEquals(0.0, episodeDto.getMagnitude());
        assertEquals(0, episodeDto.getCategory());
    }

    @Test
    public void eventListDtoTestNulls() {
        EventApiEventDto event = testEvent();
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getLocation(), dto.getLocation());
        assertNull(dto.getAffectedPopulation());
        assertNull(dto.getOsmGaps());
        assertNull(dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventListDtoTestEmptyCollections() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.setUrls(new ArrayList<>());
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getLocation(), dto.getLocation());
        assertNull(dto.getAffectedPopulation());
        assertNull(dto.getOsmGaps());
        assertNull(dto.getSettledArea());
        assertTrue(dto.getExternalUrls().isEmpty());
    }

    @Test
    public void eventListDtoTest() {
        EventApiEventDto event = testEvent();
        event.setEventDetails(new HashMap<>());
        event.getEventDetails().put("populatedAreaKm2", 100.12);
        event.getEventDetails().put("osmGapsPercentage", 30);
        event.getEventDetails().put("population", 50);
        event.getEventDetails().put("loss", 531.14);
        event.setUrls(new ArrayList<>());
        event.getUrls().add("http://google.com");
        EventListDto dto = EventListEventDtoConverter.convert(event);

        assertEquals(event.getLocation(), dto.getLocation());
        assertEquals(event.getDescription(), dto.getDescription());
        assertEquals(100.12, dto.getSettledArea());
        assertEquals(30, dto.getOsmGaps());
        assertEquals(50, dto.getAffectedPopulation());
        assertEquals(531, dto.getLoss());
        assertEquals(1, dto.getExternalUrls().size());
        assertEquals("http://google.com", dto.getExternalUrls().get(0));
    }

    @Test
    public void eventListDtoTestNames() {
        EventApiEventDto event = testEvent();
        event.setType(OTHER.toString());
        //1. type = other & properName != null
        event.setProperName("some name");
        event.getEpisodes().get(0).setType(OTHER.toString());
        EventListDto dto = EventListEventDtoConverter.convert(event);
        assertEquals("some name", dto.getEventName());

        //2. type != other & propername != null
        event.setProperName("some name");
        event.setType(DROUGHT.toString());
        event.getEpisodes().get(0).setType(CYCLONE.toString());
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(DROUGHT.getName() + " some name", dto.getEventName());

        //3. propername == null
        event.setProperName(null);
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(DROUGHT.getName(), dto.getEventName());
    }

    @Test
    public void eventListDtoTestEpisodeCount() {
        EventApiEventDto event = testEvent();
        EventListDto dto = EventListEventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 1);

        //1. with 0 episodes
        event.setEpisodeCount(0);
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 0);
        
        //2. with 2 episodes
        event.setEpisodeCount(2);
        dto = EventListEventDtoConverter.convert(event);
        assertEquals(dto.getEpisodeCount(), 2);
    }
}
