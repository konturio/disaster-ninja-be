package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EventDto {

    private UUID eventId;
    private String eventName;
    private String description;
    private List<String> externalUrls;
    private Severity severity;
    private FeatureCollection geojson;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EventType eventType;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private FeatureCollection latestEpisodeGeojson;
    private String location;
    private Double settledArea;
    private Long affectedPopulation;
    private Long osmGaps;
    private Long loss;
    private OffsetDateTime updatedAt;
    private List<Double> bbox;
    private List<Double> centroid;
    private int episodeCount;

}
