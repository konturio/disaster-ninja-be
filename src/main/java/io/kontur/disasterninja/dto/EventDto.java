package io.kontur.disasterninja.dto;

import org.wololo.geojson.FeatureCollection;

import java.util.List;
import java.util.UUID;

public class EventDto {

    private UUID eventId;
    private String eventName;
    private List<String> externalUrls;
    private Severity severity;
    private FeatureCollection geojson;

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
}
