package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AppSummaryDto;
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

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new embedded app")
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AppDto.class)))
    @PostMapping
    public AppDto create(@Parameter(name = "app") @RequestBody AppDto appDto) {
        return userProfileClient.createApp(appDto);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete app")
    @ApiResponse(responseCode = "204")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Parameter(name = "id") UUID id) {
        userProfileClient.deleteApp(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update existing app")
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AppDto.class)))
    @PutMapping(path = "/{id}")
    public AppDto update(@PathVariable @Parameter(name = "id") UUID id,
                         @RequestBody @Parameter(name = "app") AppDto appDto) {
        return userProfileClient.updateApp(id, appDto);
    }

    @Operation(summary = "Get application information by id")
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AppDto.class)))
    @GetMapping(path = "/{id}")
    public AppDto get(@PathVariable @Parameter(name = "id",
        example = "58851b50-9574-4aec-a3a6-425fa18dcb54") //DN2_ID, but must be constant here
                          UUID id) {
        return userProfileClient.getApp(id);
    }

    @Operation(summary = "Get application list available to user (includes public apps"
        + " and user-owned apps)")
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = AppSummaryDto.class))))
    @GetMapping
    public List<AppSummaryDto> getList() {
        return userProfileClient.getAppsList();
    }
}
