package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

import java.util.List;
import java.util.UUID;

@Data
public class EventDto {

    private UUID eventId;
    private String eventName;
    private List<String> externalUrls;
    private Severity severity;
    private FeatureCollection geojson;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EventType eventType;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private FeatureCollection latestEpisodeGeojson;

}
