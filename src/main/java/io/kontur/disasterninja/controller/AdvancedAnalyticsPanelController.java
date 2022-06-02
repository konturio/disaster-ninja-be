package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.AdvancedAnalyticsDto;
import io.kontur.disasterninja.dto.AdvancedAnalyticsRequestDto;
import io.kontur.disasterninja.service.AdvancedAnalyticsPanelService;
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

@Tag(name = "Advanced Analytics tab", description = "Advanced Analytics panel API")
@RestController
@RequestMapping("/advanced_polygon_details")
@RequiredArgsConstructor
public class AdvancedAnalyticsPanelController {

    private final AdvancedAnalyticsPanelService advancedAnalyticsPanelService;

    @Operation(summary = "Calculate data for advanced analytics panel using insights-api service",
            tags = {"Analytics tab"},
            description = "Calculate advanced analytics to test the concept of getting analytical data automatically so analytical data makes sense.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AdvancedAnalyticsDto.class))))
    @PostMapping("/layers")
    public List<AdvancedAnalyticsDto> getAdvancedAnalyticsTab(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format. Send polygon as FeatureCollection")
                                                              @RequestBody AdvancedAnalyticsRequestDto advancedAnalyticsRequest) {
        return advancedAnalyticsPanelService.calculateAnalytics(advancedAnalyticsRequest);
    }

    @Operation(summary = "Calculate data for advanced analytics panel using insights-api service",
            tags = {"Analytics tab"},
            description = "Calculate advanced analytics to test the concept of getting analytical data automatically so analytical data makes sense.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AdvancedAnalyticsDto.class))))
    @PostMapping
    public List<AdvancedAnalyticsDto> getAdvancedAnalyticsTab(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format. Send polygon as FeatureCollection")
            @RequestBody(required = false) GeoJSON geoJSON) {
        return advancedAnalyticsPanelService.calculateAnalytics(new AdvancedAnalyticsRequestDto(null, geoJSON));
    }

}
