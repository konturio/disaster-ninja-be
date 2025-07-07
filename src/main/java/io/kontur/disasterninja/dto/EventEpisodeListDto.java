package io.kontur.disasterninja.dto;

import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.FeatureCollection;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class EventEpisodeListDto {

    private String name;
    private List<String> externalUrls;
    private Severity severity;
    private Double magnitude;
    private Integer category;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
    private OffsetDateTime updatedAt;
    private FeatureCollection geojson;
    private String location;

}
