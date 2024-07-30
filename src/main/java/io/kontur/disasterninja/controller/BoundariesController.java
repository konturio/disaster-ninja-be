package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.LayersApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Point;

import java.util.List;

@Tag(name = "Boundaries", description = "Boundaries API")
@RestController
@RequestMapping("/boundaries")
@RequiredArgsConstructor
public class BoundariesController {

    private final LayersApiService layersApiService;
    private final GeometryTransformer geometryTransformer;

    @Operation(summary = "Returns boundaries for selected point using LayersApi service",
            tags = {"Boundaries"},
            description = "Returns boundaries for selected point using kcApi service")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeatureCollection.class)))
    @PostMapping
    public ResponseEntity<FeatureCollection> getBoundaries(@io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "Point in GeoJSON format. Send point as FeatureCollection or Feature or Point")
                                           @RequestBody GeoJSON geoJSON) {
        Point point = geometryTransformer.getPointFromGeometry(geometryTransformer.getGeometryFromGeoJson(geoJSON));
        List<Feature> features = layersApiService.getFeatures(point, "konturBoundaries", null);
        if (features == null || features.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new FeatureCollection(new Feature[]{}));
        }
        return ResponseEntity.ok(new FeatureCollection(features.toArray(Feature[]::new)));
    }
}
