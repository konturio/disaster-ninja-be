package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixDto;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixRequestDto;
import io.kontur.disasterninja.service.BivariateMatrixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bivariate matrix", description = "Bivariate matrix API")
@RestController
@RequestMapping("/bivariate_matrix")
public class BivariateMatrixController {

    private final BivariateMatrixService bivariateMatrixService;

    private final ObjectMapper objectMapper;

    public BivariateMatrixController(BivariateMatrixService bivariateMatrixService) {
        this.bivariateMatrixService = bivariateMatrixService;
        this.objectMapper = JsonMapper
                .builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build()
                .setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Operation(summary = "Calculate data for bivariate matrix using insights-api service",
            tags = {"Bivariate matrix"},
            description = "Calculate bivariate matrix for given geometry and important layers.")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BivariateMatrixDto.class)))
    @PostMapping
    @PreAuthorize("hasRole('bivariateStatistic')")
    public BivariateMatrixDto getBivariateMatrix(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format. Send polygon as FeatureCollection")
            @RequestBody(required = false) BivariateMatrixRequestDto bivariateMatrixRequestDto) {

        var resp = bivariateMatrixService.getDataForBivariateMatrix(bivariateMatrixRequestDto);
        return resp;
    }
}