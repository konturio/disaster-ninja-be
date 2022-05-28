package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.TileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tiles", description = "Tiles API")
@RestController
@RequestMapping("/tiles")
@RequiredArgsConstructor
public class TileController {

    private final TileService tileService;

    @Operation(summary = "Returns mvt tile using z, x, y and Insights API service",
            tags = {"Tiles"},
            description = "Returns mvt tile using z, x, y and Insights API service")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/vnd.mapbox-vector-tile", schema = @Schema(implementation = byte[].class)))
    @GetMapping(value = "/{z}/{x}/{y}.mvt", produces = "application/vnd.mapbox-vector-tile")
    public byte[] getTileMvt(@PathVariable Integer z, @PathVariable Integer x, @PathVariable Integer y){
        return tileService.getTileMvt(z, x, y);
    }
}
