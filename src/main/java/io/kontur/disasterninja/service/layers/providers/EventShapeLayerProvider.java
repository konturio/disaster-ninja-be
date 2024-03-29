package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.config.logging.LogHttpTraceRepository.LOG;
import static io.kontur.disasterninja.domain.enums.LayerType.GEOJSON;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getJtsGeometry;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getPreparedGeometryFromRequest;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class EventShapeLayerProvider implements LayerProvider {

    public static final String EVENT_SHAPE_LAYER_ID = "eventShape";
    private final EventApiService eventApiService;

    @Override
    public CompletableFuture<List<Layer>> obtainGlobalLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<Layer>> obtainUserLayers(LayerSearchParams searchParams) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    @Timed(value = "layers.obtainSelectedAreaLayers", histogram = true)
    public CompletableFuture<List<Layer>> obtainSelectedAreaLayers(LayerSearchParams searchParams) {
        if (searchParams.getEventId() == null || searchParams.getEventFeed() == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Layer layer = obtainLayer(EVENT_SHAPE_LAYER_ID, searchParams);
        return CompletableFuture.completedFuture(layer == null ? Collections.emptyList() : List.of(layer));
    }

    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (searchParams.getEventId() == null || searchParams.getEventFeed() == null) {
            throw new WebApplicationException("EventId and EventFeed must be provided when requesting layer " + layerId,
                    HttpStatus.BAD_REQUEST);
        }
        EventDto eventDto = eventApiService.getEvent(searchParams.getEventId(), searchParams.getEventFeed());

        Layer layer = fromEventDto(eventDto);
        if (layer != null && layer.getSource() != null && layer.getSource().getData() != null) {
            if (!isIntersectsWithGeometry(layer.getSource().getData().getFeatures(), searchParams.getBoundary())) {
                LOG.info("No features intersecting with requested boundary {}", searchParams.getBoundary());
                return null;
            }
        }
        return layer;
    }

    private Layer fromEventDto(EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }

        return Layer.builder()
                .eventType(eventDto.getEventType())
                .id(EVENT_SHAPE_LAYER_ID)
                .source(LayerSource.builder()
                        .type(GEOJSON)
                        .data(eventDto.getLatestEpisodeGeojson()) //sic!
                        .build())
                .eventIdRequiredForRetrieval(true)
                .build();
    }

    @Override
    public boolean isApplicable(String layerId) {
        return EVENT_SHAPE_LAYER_ID.equals(layerId);
    }

    private boolean isIntersectsWithGeometry(Feature[] input, Geometry geoJson) {
        if (input == null) {
            return false;
        }
        if (geoJson == null) {
            return true;
        }
        PreparedGeometry jtsGeometry = getPreparedGeometryFromRequest(geoJson);

        //filter items by geoJson Geometry
        return Arrays.stream(input)
                .anyMatch(json -> {
                    Geometry featureGeom = json.getGeometry();
                    return featureGeom == null || //include items without geometry ("global" ones)
                            jtsGeometry.intersects(getJtsGeometry(featureGeom));
                });
    }
}
