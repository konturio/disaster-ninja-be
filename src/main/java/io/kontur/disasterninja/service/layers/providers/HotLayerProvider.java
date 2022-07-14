package io.kontur.disasterninja.service.layers.providers;

import io.kontur.disasterninja.client.KcApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.domain.LayerSource;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.kontur.disasterninja.client.KcApiClient.HOT_PROJECTS;
import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerSourceType.GEOJSON;
import static io.kontur.disasterninja.service.layers.providers.OsmLayerProvider.getFeatureProperty;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Service
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HotLayerProvider implements LayerProvider {

    public static final String HOT_LAYER_ID = "hotProjects";
    private final KcApiClient kcApiClient;

    @Override
    @Timed(value = "layers.getLayersList", percentiles = {0.5, 0.75, 0.9, 0.99})
    public CompletableFuture<List<Layer>> obtainLayers(LayerSearchParams searchParams) {
        if (searchParams.getBoundary() == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Layer layer = obtainLayer(HOT_LAYER_ID, searchParams);
        return CompletableFuture.completedFuture(layer == null ? Collections.emptyList() : List.of(layer));
    }

    /**
     * @return layer containing features whose Centroid intersects with requested geoJson (same as in DN1)
     */
    @Override
    public Layer obtainLayer(String layerId, LayerSearchParams searchParams) {
        if (!isApplicable(layerId)) {
            return null;
        }
        if (searchParams.getBoundary() == null) {
            throw new WebApplicationException("GeoJson boundary must be specified for layer " + layerId,
                    HttpStatus.BAD_REQUEST);
        }

        //it's possible that the centroid does intersect with the geoJson, but geometries themselves do not!
        List<Feature> hotProjectLayers = kcApiClient.getCollectionItemsByCentroidGeometry(searchParams.getBoundary(),
                HOT_PROJECTS);
        return fromHotProjectLayers(hotProjectLayers);
    }

    @Override
    public boolean isApplicable(String layerId) {
        return HOT_LAYER_ID.equals(layerId);
    }

    /**
     * A single layer is constructed from all <b>dto</b> features
     */
    Layer fromHotProjectLayers(List<Feature> dto) {
        if (dto == null || dto.isEmpty()) {
            return null;
        }
        //The entire collection is one layer
        Layer.LayerBuilder builder = Layer.builder()
                .id(HOT_LAYER_ID)
                .source(LayerSource.builder() //source is required to calculate this layers legend
                        .type(GEOJSON)
                        //enrich features with HOT Project URL
                        .data(new FeatureCollection(dto.stream().peek(f -> {
                            if (f.getProperties() != null) {
                                Integer projectId = getFeatureProperty(f, PROJECT_ID, Integer.class);
                                if (projectId != null) {
                                    f.getProperties().put(PROJECT_LINK, HOT_PROJECTS_URL + projectId);
                                }
                            }
                        }).toArray(Feature[]::new)))
                        .build());
        return builder.build();
        //the rest params are set by LayerConfigService
    }
}
