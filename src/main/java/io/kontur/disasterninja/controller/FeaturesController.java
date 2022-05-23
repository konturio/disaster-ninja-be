package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.FeatureDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(FeaturesController.PATH)
public class FeaturesController {

    public static final String PATH = "/features";
    private final UserProfileClient userProfileClient;

    @Operation(tags = "Features", summary =
            "Get features for app id allowed for user by username retrieved from token "
                    + "(including public ones)")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = FeatureDto.class))))
    @GetMapping
    public List<FeatureDto> getUserAppFeatures(
            @RequestParam(name = "appId") @Parameter(name = "appId") UUID appId) {
        return userProfileClient.getUserAppFeatures(appId);
    }
}
