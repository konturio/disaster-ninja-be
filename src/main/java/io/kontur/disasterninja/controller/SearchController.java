package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.InsightsLLMApiClient;
import io.kontur.disasterninja.dto.SearchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name = "Search", description = "Search Panel API")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SearchController {

    private final InsightsLLMApiClient llmAnalyticsClient;

    @Operation(summary = "search",
            tags = {"Search"},
            description = "Geocode locations, search layers, indicators, events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request: invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated"),
    })
    @GetMapping()
    public SearchDto search(
                @RequestParam(name = "appId", required = true) UUID appId,
                @RequestParam(name = "query", required = true) String query) {
        return llmAnalyticsClient.search(appId, query);
    }

}
