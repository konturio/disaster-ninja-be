package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.communities.CommunityDto;
import io.kontur.disasterninja.service.communities.CommunitiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wololo.geojson.GeoJSON;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class CommunitiesController {
    private final CommunitiesService communitiesService;

    @Operation(tags = "Communities", summary = "Get List of communities")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = CommunityDto.class))))
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<CommunityDto> getCommunities(@RequestBody
                                             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                 description = "geoJSON: only communities with geometry intersecting with" +
                                                     " geoJSON boundary will be returned")
                                                 GeoJSON geoJSON) {

        return communitiesService.getCommunitiesByGeometry(geoJSON);
    }

}
