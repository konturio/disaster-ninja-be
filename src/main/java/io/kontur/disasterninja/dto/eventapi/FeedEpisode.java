package io.kontur.disasterninja.dto.eventapi;

import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.Severity;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public FeatureCollection getGeometries() {
        return geometries;
    }

    public void setGeometries(FeatureCollection geometries) {
        this.geometries = geometries;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public void setSourceUpdatedAt(OffsetDateTime sourceUpdatedAt) {
        this.sourceUpdatedAt = sourceUpdatedAt;
    }

    public Map<String, Object> getEpisodeDetails() {
        return episodeDetails;
    }

    public void setEpisodeDetails(Map<String, Object> episodeDetails) {
        this.episodeDetails = episodeDetails;
    }

    @Override
    public String toString() {
        return "FeedEpisode{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", active=" + active +
                ", severity=" + severity +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                ", updatedAt=" + updatedAt +
                ", sourceUpdatedAt=" + sourceUpdatedAt +
                ", geometries=" + geometries +
                ", episodeDetails=" + episodeDetails +
                '}';
    }

    public Set<UUID> getObservations() {
        return observations;
    }

    public void setObservations(Set<UUID> observations) {
        this.observations = observations;
    }

    public void addObservation(UUID observations) {
        this.observations.add(observations);
    }
}
