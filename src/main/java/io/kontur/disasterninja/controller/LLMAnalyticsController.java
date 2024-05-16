package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.client.InsightsLLMApiClient;
import io.kontur.disasterninja.dto.AnalyticsRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "LLM Analytics", description = "LLM Analytics Panel API")
@RestController
@RequestMapping("/llm_analytics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LLMAnalyticsController {

    private final InsightsLLMApiClient llmAnalyticsClient;

    @Operation(summary = "Get textual analytics using insights-llm-api service",
            tags = {"LLM Analytics"},
            description = "Get textual analytics using insights-llm-api service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved analytics"),
        @ApiResponse(responseCode = "400", description = "Bad Request: invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden: User is not allowed to retrieve LLM analytics"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error: Error processing the request")
    })
    @PostMapping()
    public ResponseEntity<Object> getLLMAnalytics(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Polygon in GeoJSON format and appId as UUID")
            @RequestBody AnalyticsRequestDto llmAnalyticsRequest) {
        return llmAnalyticsClient.getLLMAnalytics(llmAnalyticsRequest);
    }

}
