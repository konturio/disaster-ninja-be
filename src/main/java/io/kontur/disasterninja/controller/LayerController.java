package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsInputDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.service.layers.LayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping("layers")
public class LayerController {
    @Autowired
    LayerService layerService;
    @Autowired
    ObjectMapper objectMapper;

    @Operation(tags = "Layers", summary = "Get List of available layers")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getSummaries(@RequestBody
                                              @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                  description = "geoJSON: only layers with features intersecting with" +
                                                      " geoJSON boundary will be returned and id: EventId for" +
                                                      " EventShape layer")
                                                  LayerSummaryInputDto inputDto) {
        return layerService.getList(inputDto.getGeoJSON(), inputDto.getId())
            .stream().map(LayerSummaryDto::fromLayer)
            .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Get Layers by their ids")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json",
        array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = "/details", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerDetailsDto> getDetails(@RequestBody
                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                description = "geoJSON: only layers with features intersecting with" +
                                                    " geoJSON boundary will be returned; eventId: Event Id for" +
                                                    " EventShape layer; List of layer ids to retrieve")
                                                LayerDetailsInputDto inputDto) {
        return layerService.get(inputDto.getGeoJSON(), inputDto.getLayerIds(), inputDto.getEventId())
            .stream().map(LayerDetailsDto::fromLayer)
            .collect(Collectors.toList());
    }
}
