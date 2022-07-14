package io.kontur.disasterninja.dto.eventapi;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.*;

@Data
public class EventApiEventDto {

    private UUID eventId;
    private Long version;
    private String name;
    private String properName;
    private String description;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private List<FeedEpisode> episodes = new ArrayList<>();
    private Map<String, Object> eventDetails = new HashMap<>();
    private List<String> urls;
    private String location;
}
