package io.kontur.disasterninja.dto.eventapi;

import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.*;

public class EventDto {

    private UUID eventId;
    private Long version;
    private String name;
    private String description;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private Set<UUID> observations;
    private List<FeedEpisode> episodes = new ArrayList<>();
    private Map<String, Object> eventDetails = new HashMap<>();
    private FeatureCollection geometries;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<UUID> getObservations() {
        return observations;
    }

    public void setObservations(Set<UUID> observations) {
        this.observations = observations;
    }

    public List<FeedEpisode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<FeedEpisode> episodes) {
        this.episodes = episodes;
    }

    public Map<String, Object> getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(Map<String, Object> eventDetails) {
        this.eventDetails = eventDetails;
    }

    public FeatureCollection getGeometries() {
        return geometries;
    }

    public void setGeometries(FeatureCollection geometries) {
        this.geometries = geometries;
    }
}
