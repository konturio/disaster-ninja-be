package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.application.UpsAppDto;
import io.kontur.disasterninja.dto.application.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.application.AppSummaryDto;
import io.kontur.disasterninja.dto.application.AppContextDto;
import io.kontur.disasterninja.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(AppsController.PATH)
public class AppsController {

    public static final String PATH = "/apps";
    private final UserProfileClient userProfileClient;
    private final LayersApiClient layersApiClient;
    private final ApplicationService applicationService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new embedded app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpsAppDto.class)))
    @PostMapping
    public UpsAppDto create(@Parameter(name = "app") @RequestBody UpsAppDto upsAppDto) {
        return userProfileClient.createApp(upsAppDto);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete app", tags = {"Applications"})
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Parameter(name = "id") UUID id) {
        userProfileClient.deleteApp(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update existing app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpsAppDto.class)))
    @PutMapping(path = "/{id}")
    public UpsAppDto update(@PathVariable @Parameter(name = "id") UUID id,
                            @RequestBody @Parameter(name = "app") UpsAppDto upsAppDto) {
        return userProfileClient.updateApp(id, upsAppDto);
    }

    @Operation(summary = "Get default app id", tags = {"Applications"}, deprecated = true)
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    @GetMapping(path = "/default_id")
    public ResponseEntity<String> getDefaultAppId() {
        return userProfileClient.getDefaultAppId();
    }

    @Operation(summary = "Get application information by id", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpsAppDto.class)))
    @GetMapping(path = "/{id}")
    public UpsAppDto get(@PathVariable @Parameter(name = "id") UUID id) {
        return userProfileClient.getApp(id);
    }

    @Operation(summary = "Get application list available to user (includes public apps"
            + " and user-owned apps)", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AppSummaryDto.class))))
    @GetMapping
    public List<AppSummaryDto> getList() {
        return userProfileClient.getAppsList();
    }

    @Operation(summary = "Get list of default layers for the app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Layer.class))))
    @GetMapping("/{id}/layers")
    public List<Layer> getListOfLayers(@PathVariable("id") UUID appId) {
        return layersApiClient.getApplicationLayers(appId);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Updates list of layers for the app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Layer.class))))
    @PutMapping("/{id}/layers")
    public List<Layer> updateListOfLayers(@PathVariable("id") UUID appId,
                                          @RequestBody List<AppLayerUpdateDto> layers) {
        return layersApiClient.updateApplicationLayers(appId, layers);
    }

    @Operation(summary = "Get application context by id", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppContextDto.class)))
    @GetMapping("/context")
    public AppContextDto getAppContext(@RequestParam(value = "appId", required = false) UUID appId) {
        return applicationService.getAppContext(appId);
    }
}
