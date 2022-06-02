package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.AnalyticsDto;
import io.kontur.disasterninja.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Tag(name = "Analytics tab", description = "Analytics tab API")
@RestController
@RequestMapping("/polygon_details")
@RequiredArgsConstructor
public class AnalyticsTabController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Calculate data for analytics tab using insights-api service",
            tags = {"Analytics tab"},
            description = "Calculate data for analytics tab using insights-api service")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AnalyticsDto.class))))
    @PostMapping
    public List<AnalyticsDto> getAnalyticsTab(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format. Send polygon as FeatureCollection")
            @RequestBody GeoJSON geoJSON) {
        return analyticsService.calculateAnalyticsForPanel(geoJSON);
    }
}
