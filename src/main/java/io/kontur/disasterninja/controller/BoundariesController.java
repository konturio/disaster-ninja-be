package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.BoundariesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wololo.geojson.FeatureCollection;

@Tag(name = "Boundaries", description = "Boundaries API")
@RestController
@RequestMapping("/boundaries")
@RequiredArgsConstructor
public class BoundariesController {

    private final BoundariesService boundariesService;

    @Operation(summary = "Returns boundaries for selected point using kcApi service",
            tags = {"Boundaries"},
            description = "Returns boundaries for selected point using kcApi service")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeatureCollection.class)))
    @PostMapping
    public FeatureCollection getBoundaries(@io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "Point in GeoJSON format. Send point as FeatureCollection or Feature or Point")
                                           @RequestBody String geoJsonString) {
        return boundariesService.getBoundaries(geoJsonString);
    }
}
