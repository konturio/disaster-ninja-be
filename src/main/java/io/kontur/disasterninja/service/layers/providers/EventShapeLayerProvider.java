package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.service.EventApiService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Geometry;

import java.util.Arrays;
import java.util.List;

import static io.kontur.disasterninja.config.logging.LogHttpTraceRepository.LOG;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getJtsGeometry;
import static io.kontur.disasterninja.service.converter.GeometryConverter.getPreparedGeometryFromRequest;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class EventShapeLayerProvider implements LayerProvider {
    private final EventApiService eventApiService;

    @Override
    public List<Layer> obtainLayers(LayerSearchParams searchParams) {
        if (searchParams.getEventId() == null || searchParams.getEventFeed() == null) {
            return null;
        }
        Layer layer = obtainLayer(EVENT_SHAPE_LAYER_ID, searchParams);
        return layer == null ? null : List.of(layer);
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
            Feature[] filteredFeatures = filterFeaturesByGeometry(layer.getSource().getData().getFeatures(), searchParams.getBoundary());
            if (filteredFeatures.length == 0) {
                LOG.info("No features intersecting with requested boundary {}", searchParams.getBoundary());
                return null;
            }
            layer.getSource().setData(new FeatureCollection(filteredFeatures));
        }
        return layer;
    }

    Layer fromEventDto(EventDto eventDto) {
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

    private Feature[] filterFeaturesByGeometry(Feature[] input, Geometry geoJson) {
        if (input == null) {
            return new Feature[]{};
        }
        if (geoJson == null) {
            return input;
        }
        PreparedGeometry jtsGeometry = getPreparedGeometryFromRequest(geoJson);

        //filter items by geoJson Geometry
        return Arrays.stream(input)
            .filter(json -> {
                Geometry featureGeom = json.getGeometry();
                return featureGeom == null || //include items without geometry ("global" ones)
                    jtsGeometry.intersects(getJtsGeometry(featureGeom));
            })
            .toArray(Feature[]::new);
    }
}
