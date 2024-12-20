package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.TileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tiles", description = "Tiles API")
@RestController
@RequestMapping("/tiles")
@RequiredArgsConstructor
public class TileController {

    private final TileService tileService;

    @Operation(summary = "Returns bivariate mvt tile using z, x, y, indicator class and Insights API service.",
            tags = {"Tiles"},
            description = "Returns bivariate mvt tile using z, x, y, indicator class and Insights API service.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/vnd.mapbox-vector-tile", schema = @Schema(implementation = byte[].class)))
    @GetMapping(value = "/bivariate/v1/{z}/{x}/{y}.mvt", produces = "application/vnd.mapbox-vector-tile")
    public ResponseEntity<byte[]> getBivariateTileMvt(@PathVariable Integer z, @PathVariable Integer x, @PathVariable Integer y,
            @RequestParam(defaultValue = "all") String indicatorsClass,
            @RequestParam(required = false) String indicators){
        return tileService.getBivariateTileMvt(z, x, y, indicatorsClass, indicators);
    }

    @Operation(summary = "Returns bivariate mvt tile using z, x, y, indicator class and Insights API service version 2.",
            tags = {"Tiles"},
            description = "Returns bivariate mvt tile using z, x, y, indicator class and Insights API service version 2.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/vnd.mapbox-vector-tile", schema = @Schema(implementation = byte[].class)))
    @GetMapping(value = "/bivariate/v2/{z}/{x}/{y}.mvt", produces = "application/vnd.mapbox-vector-tile")
    public ResponseEntity<byte[]> getBivariateTileMvtV2(@PathVariable Integer z, @PathVariable Integer x, @PathVariable Integer y,
                                                      @RequestParam(defaultValue = "all") String indicatorsClass){
        return tileService.getBivariateTileMvtV2(z, x, y, indicatorsClass);
    }
}
