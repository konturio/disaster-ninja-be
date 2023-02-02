package io.kontur.disasterninja.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EventListDto {

    private UUID eventId;
    private String eventName;
    private String description;
    private String location;
    private Severity severity;
    private Long affectedPopulation;
    private Double settledArea;
    private Long osmGaps;
    private Long loss;
    private OffsetDateTime updatedAt;
    private List<String> externalUrls;
    private List<Double> bbox;
    private List<Double> centroid;

}
