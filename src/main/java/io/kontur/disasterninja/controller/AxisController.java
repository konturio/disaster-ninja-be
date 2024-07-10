package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.Transformation;
import io.kontur.disasterninja.service.AxisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import static java.util.Collections.emptyList;


@RestController
@RequestMapping("/axis")
@RequiredArgsConstructor
public class AxisController {

    private final AxisService axisService;

    @Operation(summary = "Get axis transformations by numerator and denominator UUID", tags = {"Axis"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Transformation.class))))
    @ApiResponse(responseCode = "404", description = "Axis is not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/{numerator}/{denominator}/transformations")
    public ResponseEntity<List<Transformation>> get(
            @PathVariable @Parameter(name = "numerator") UUID numerator,
            @PathVariable @Parameter(name = "denominator") UUID denominator) {
        List<Transformation> transformations = axisService.getTransformations(numerator, denominator);
        if (transformations.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyList());
        }
        return ResponseEntity.ok(transformations);
    }

    @Operation(summary = "Get axis list", tags = {"Axis"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = BivariateLegendAxisDescription.class))))
    @ApiResponse(responseCode = "204", description = "No content", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public ResponseEntity<List<BivariateLegendAxisDescription>> getList(
            @Parameter(description = "Minimum quality to filter the axes")
            @RequestParam(name = "minQuality", required = false) Double minQuality) {
        List<BivariateLegendAxisDescription> axes = axisService.getDataForAxis();
        if (axes.size() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(emptyList());
        }

        if (minQuality != null) {
            axes = axes.stream()
                       .filter(axis -> axis.getQuality() != null && axis.getQuality() >= minQuality)
                       .collect(Collectors.toList());
        }

        return ResponseEntity.ok(axes);
    }
}
