package io.kontur.disasterninja.dto.eventapi;

import io.kontur.disasterninja.dto.Severity;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.*;

@Data
public class EventApiEventDto {

    private UUID eventId;
    private Long version;
    private String name;
    private String properName;
    private String description;
    private String type;
    private Severity severity;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private FeatureCollection geometries;
    private List<FeedEpisode> episodes = new ArrayList<>();
    private Map<String, Object> eventDetails = new HashMap<>();
    private List<String> urls;
    private String location;
    private List<Double> bbox = new ArrayList<>();
    private List<Double> centroid = new ArrayList<>();
    private int episodeCount;
}
