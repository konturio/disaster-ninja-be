package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.CountriesService;
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
import org.wololo.geojson.GeoJSON;

import java.util.Set;

@Tag(name = "Countries", description = "Countries API")
@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountriesController {

    private final CountriesService countriesService;

    @Operation(summary = "Return list of ISO3 codes of countries affected by input geometry", tags = {"Countries"},
            description = "Returns list of ISO3 codes of countries for selected geometry using LayersApi service")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    @PostMapping
    public Set<String> getAffectedCountries(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                    "Geometry in GeoJSON format. Send as FeatureCollection or Feature or Geometry")
            @RequestBody GeoJSON geoJSON) {
        return countriesService.getAffectedCountries(geoJSON);
    }
}
