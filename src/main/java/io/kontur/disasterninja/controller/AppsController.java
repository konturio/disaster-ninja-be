package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AppLayerUpdateDto;
import io.kontur.disasterninja.dto.AppSummaryDto;
import io.kontur.disasterninja.service.ApplicationService;
import io.kontur.disasterninja.service.layers.LayersApiService;
import io.kontur.disasterninja.service.layers.providers.BivariateLayerProvider;
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

import javax.validation.Valid;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(AppsController.PATH)
public class AppsController {

    public static final String PATH = "/apps";
    private final UserProfileClient userProfileClient;
    private final LayersApiService layersApiService;
    private final BivariateLayerProvider bivariateLayerProvider;
    private final ApplicationService applicationService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new embedded app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppDto.class)))
    @PostMapping
    public AppDto create(@Parameter(name = "app") @RequestBody @Valid AppDto appDto) {
        return userProfileClient.createApp(appDto);
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
                    schema = @Schema(implementation = AppDto.class)))
    @PutMapping(path = "/{id}")
    public AppDto update(@PathVariable @Parameter(name = "id") UUID id,
                         @RequestBody @Parameter(name = "app") @Valid AppDto appDto) {
        return userProfileClient.updateApp(id, appDto);
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
                    schema = @Schema(implementation = AppDto.class)))
    @GetMapping(path = "/{id}")
    public AppDto get(@PathVariable @Parameter(name = "id") UUID id) {
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
        List<Layer> layers = layersApiService.getApplicationLayers(appId);

        //TODO This is bad. To be removed during US1544 Serve bivariate layers via Layers API
        ListIterator<Layer> iterator = layers.listIterator();
        while (iterator.hasNext()) {
            Layer next = iterator.next();
            if (bivariateLayerProvider.isApplicable(next.getId())) {
                iterator.set(bivariateLayerProvider.obtainLayer(next.getId(), null));
            }
        }

        return layers;
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Updates list of layers for the app", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Layer.class))))
    @PutMapping("/{id}/layers")
    public List<Layer> updateListOfLayers(@PathVariable("id") UUID appId,
                                          @RequestBody List<AppLayerUpdateDto> layers) {
        return layersApiService.updateApplicationLayers(appId, layers);
    }

    @Operation(summary = "Get application config with features and user settings by id. Returns default app if no appId is provided", tags = {"Applications"})
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppDto.class)))
    @GetMapping(path = "/configuration")
    public AppDto getAppConfig(@RequestParam(name = "appId", required = false) UUID appId) {
        return applicationService.getAppConfig(appId);
    }
}
