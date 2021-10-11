package io.kontur.disasterninja.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EventListEventDto {

    private UUID eventId;
    private String eventName;
    private String location;
    private Severity severity;
    private Long affectedPopulation;
    private Long settledArea;
    private Long osmGaps;
    private OffsetDateTime updatedAt;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Long getAffectedPopulation() {
        return affectedPopulation;
    }

    public void setAffectedPopulation(Long affectedPopulation) {
        this.affectedPopulation = affectedPopulation;
    }

    public Long getSettledArea() {
        return settledArea;
    }

    public void setSettledArea(Long settledArea) {
        this.settledArea = settledArea;
    }

    public Long getOsmGaps() {
        return osmGaps;
    }

    public void setOsmGaps(Long osmGaps) {
        this.osmGaps = osmGaps;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
