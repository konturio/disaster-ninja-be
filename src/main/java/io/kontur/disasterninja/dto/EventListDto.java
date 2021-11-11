package io.kontur.disasterninja.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EventListDto {
    private UUID eventId;
    private String eventName;
    private String locations;
    private Severity severity;
    private Long affectedPopulation;
    private Double settledArea;
    private Long osmGaps;
    private OffsetDateTime updatedAt;
    private List<String> externalUrls;
}
