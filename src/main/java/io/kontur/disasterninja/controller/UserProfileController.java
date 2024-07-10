package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.ActiveSubscriptionDto;
import io.kontur.disasterninja.dto.ActiveSubscriptionRequestDto;
import io.kontur.disasterninja.dto.UserDto;
import io.kontur.disasterninja.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(UserProfileController.PATH)
@RequiredArgsConstructor
public class UserProfileController {

    public static final String PATH = "/users";

    private final UserProfileService userProfileService;

    @Operation(tags = "Users", summary = "Get current user")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current_user")
    public UserDto getCurrentUser() {
        return userProfileService.getCurrentUser();
    }

    @Operation(tags = "Users", summary = "Update current user")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserDto.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/current_user")
    public UserDto updateCurrentUser(@RequestBody @Parameter(name = "user") UserDto userDto) {
        return userProfileService.updateUser(userDto);
    }

    @Operation(tags = "Users", summary = "Get current user active billing subscription for application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Active subscription found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ActiveSubscriptionDto.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current_user/billing_subscription")
    public ResponseEntity<ActiveSubscriptionDto> getActiveSubscription(@RequestParam(name = "appId", required = true) UUID appId) {
        return userProfileService.getActiveSubscription(appId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(tags = "Users", summary = "Set current user active billing subscription for application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Active subscription set",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ActiveSubscriptionDto.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/current_user/billing_subscription")
    public ResponseEntity<ActiveSubscriptionDto> setActiveSubscription(
            @Valid @RequestBody(required = true) ActiveSubscriptionRequestDto dto
    ) {
        return ResponseEntity.ok(userProfileService.setActiveSubscription(dto.getAppId(), dto.getBillingPlanId(), dto.getBillingSubscriptionId()));
    }
}
