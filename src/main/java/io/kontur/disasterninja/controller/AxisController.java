package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.service.AxisService;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/axes")
@RequiredArgsConstructor
public class AxisController {

    private final AxisService axisService;

    @Operation(summary = "Get axis information by numerator and denominator UUID", tags = {"Axis"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BivariateLegendAxisDescription.class)))
    @GetMapping(path = "/{numerator}/{denominator}")
    public BivariateLegendAxisDescription get(
            @PathVariable @Parameter(name = "numerator") UUID numerator,
            @PathVariable @Parameter(name = "denominator") UUID denominator) {
        List<BivariateLegendAxisDescription> axes = axisService.getDataForAxis(numerator, denominator);
        return axes.size() > 0 ? axes.get(0) : null;
    }

    @Operation(summary = "Get axis list", tags = {"Axis"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = BivariateLegendAxisDescription.class))))
    @GetMapping
    public List<BivariateLegendAxisDescription> getList() {
        return axisService.getDataForAxis(null, null);
    }
}
