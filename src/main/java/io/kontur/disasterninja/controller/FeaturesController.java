package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.FeatureDto;
import io.kontur.disasterninja.service.LiveSensorFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.wololo.geojson.FeatureCollection;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(FeaturesController.PATH)
public class FeaturesController {

    public static final String PATH = "/features";
    private final UserProfileClient userProfileClient;
    private final LiveSensorFeatureService liveSensorFeatureService;

    @Operation(tags = "Features", summary =
            "Get features for app id allowed for user by username retrieved from token "
                    + "(including public ones)", deprecated = true)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = FeatureDto.class))))
    @GetMapping
    @Deprecated
    public List<FeatureDto> getUserAppFeatures(
            @RequestParam(name = "appId") @Parameter(name = "appId") UUID appId) {
        return userProfileClient.getUserAppFeatures(appId);
    }

    @Operation(tags = "Features", summary = "Append user's live sensor data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Append was successful")})
    @PostMapping(path = "live-sensor", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> liveSensor(@RequestBody FeatureCollection body) {
        if (body == null || body.getFeatures() == null || body.getFeatures().length == 0) {
            return ResponseEntity.badRequest().build();
        }

        liveSensorFeatureService.appendLiveSensorData(body);
        return ResponseEntity.ok().build();
    }
}
