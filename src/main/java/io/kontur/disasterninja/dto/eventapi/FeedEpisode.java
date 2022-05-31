package io.kontur.disasterninja.dto.eventapi;

import io.kontur.disasterninja.dto.Severity;
import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class FeedEpisode {

    private String name;
    private String description;
    private String type;
    private Boolean active;
    private Severity severity;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime sourceUpdatedAt;
    private Set<UUID> observations = new HashSet<>();
    private Map<String, Object> episodeDetails;
    private FeatureCollection geometries;
}
