package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wololo.geojson.FeatureCollection;

import java.util.List;
import java.util.UUID;

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

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public List<String> getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(List<String> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public FeatureCollection getGeojson() {
        return geojson;
    }

    public void setGeojson(FeatureCollection geojson) {
        this.geojson = geojson;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public FeatureCollection getLatestEpisodeGeojson() {
        return latestEpisodeGeojson;
    }

    public void setLatestEpisodeGeojson(FeatureCollection latestEpisodeGeojson) {
        this.latestEpisodeGeojson = latestEpisodeGeojson;
    }
}
