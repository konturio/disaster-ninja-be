package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixDto;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixRequestDto;
import io.kontur.disasterninja.service.BivariateMatrixService;
import io.kontur.disasterninja.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Bivariate matrix", description = "Bivariate matrix API")
@RestController
@RequestMapping("/bivariate_matrix")
@RequiredArgsConstructor
public class BivariateMatrixController {

    private final BivariateMatrixService bivariateMatrixService;
    private final UserProfileService userProfileService;

    @Operation(summary = "Calculate data for bivariate matrix using insights-api service",
            tags = {"Bivariate matrix"},
            description = "Calculate bivariate matrix for given geometry and important layers (hardcoded on front end, UI in US1414). Performs GraphQL query PolygonStatistics/bivariateStatistic. 'name' in the response corresponds to 'param_id'")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BivariateMatrixDto.class)))
    @PostMapping
    public ResponseEntity<BivariateMatrixDto> getBivariateMatrix(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format. Send polygon as FeatureCollection")
            @RequestBody(required = false) BivariateMatrixRequestDto bivariateMatrixRequestDto) {
        if (userProfileService.userHasAccessToFeature(List.of("mcda", "bivariate_color_manager", "bivariate_manager"))) {
            return ResponseEntity.ok(bivariateMatrixService.getDataForBivariateMatrix(bivariateMatrixRequestDto));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
