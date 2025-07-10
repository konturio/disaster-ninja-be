package io.kontur.disasterninja.dto.eventapi;

import io.kontur.disasterninja.dto.Severity;
import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.*;

@Getter
@Setter
public class FeedEpisode {

    private String name;
    private String description;
    private String type;
    private String location;
    private Boolean active;
    private Severity severity;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime sourceUpdatedAt;
    private Set<UUID> observations = new HashSet<>();
    private Map<String, Object> episodeDetails;
    private Map<String, Object> severityData = new HashMap<>();
    private FeatureCollection geometries;
    private List<String> urls;
}
