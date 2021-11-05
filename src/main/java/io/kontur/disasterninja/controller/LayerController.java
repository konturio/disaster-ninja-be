package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.service.layers.LayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping("layers")
public class LayerController {
    @Autowired
    LayerService layerService;
    @Autowired
    ObjectMapper objectMapper;

    @Operation(summary = "Get List of available layers")
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

    @Operation(summary = "Get Layers by their ids")
    @GetMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<LayerDetailsDto> getDetails(@Parameter(description = "List of layer ids to retrieve") @RequestParam List<String> layerIds,
                                            @Parameter(description = "EventId for EventShape layer") @RequestParam UUID eventId) {
        return layerService.get(layerIds, eventId)
            .stream().map(LayerDetailsDto::fromLayer)
            .collect(Collectors.toList());
    }

}
