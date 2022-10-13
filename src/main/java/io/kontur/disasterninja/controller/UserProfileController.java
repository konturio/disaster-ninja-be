package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.UserDto;
import io.kontur.disasterninja.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
