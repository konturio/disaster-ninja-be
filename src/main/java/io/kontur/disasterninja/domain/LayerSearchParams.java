package io.kontur.disasterninja.domain;

import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.Geometry;

import java.util.UUID;

@Data
@Builder
public class LayerSearchParams {

    private final Geometry boundary;
    private final UUID appId;
    private final UUID eventId;
    private final String eventFeed;
    @Builder.Default
    private Integer limit = null;
    @Builder.Default
    private Integer offset = null;
    @Builder.Default
    private String order = null;


    public LayerSearchParams getCopyWithoutBoundary() {
        return LayerSearchParams.builder()
                .appId(appId)
                .eventId(eventId)
                .eventFeed(eventFeed)
                .limit(limit)
                .offset(offset)
                .order(order)
                .build();
    }
}
