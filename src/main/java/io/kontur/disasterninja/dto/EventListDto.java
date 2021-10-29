package io.kontur.disasterninja.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EventListDto {

    private UUID eventId;
    private String eventName;
    private String location;
    private Severity severity;
    private Long affectedPopulation;
    private Long settledArea;
    private Long osmGaps;
    private OffsetDateTime updatedAt;

}
