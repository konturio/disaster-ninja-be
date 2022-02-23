package io.kontur.disasterninja.domain;

import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.Geometry;

import java.util.UUID;

@Data
@Builder
public class LayerSearchParams {
    private final Geometry boundary;
    private final UUID eventId;
    private final String eventFeed;

    public LayerSearchParams getCopyWithoutBoundary() {
        return LayerSearchParams.builder()
            .eventId(eventId)
            .eventFeed(eventFeed)
            .build();
    }
}
