package io.kontur.disasterninja.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsSearchDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummarySearchDto;
import io.kontur.disasterninja.service.layers.LayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(LayerController.PATH)
public class LayerController {
    public static final String PATH = "/layers";
    public static final String PATH_SEARCH = "/search";
    public static final String PATH_DETAILS = "/details";
    @Autowired
    LayerService layerService;
    @Autowired
    ObjectMapper objectMapper;

    @Operation(tags = "Layers", summary = "Create a new layer")
    @ApiResponse(responseCode = "200", description = "Successfully created a layer", content = @Content(
        mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LayerSummaryDto.class)))
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public LayerSummaryDto create(@RequestBody
                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                      description = "Layer data")
                                      LayerCreateDto dto) {
        Layer layer = layerService.create(dto);
        return LayerSummaryDto.fromLayer(layer);
    }

    @Operation(tags = "Layers", summary = "Update an existing new layer")
    @ApiResponse(responseCode = "200", description = "Successfully updated a layer", content = @Content(
        mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LayerSummaryDto.class)))
    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public LayerSummaryDto update(@PathVariable String id, @RequestBody
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                          description = "Layer data") LayerUpdateDto dto) {
        Layer layer = layerService.update(id, dto);
        return LayerSummaryDto.fromLayer(layer);
    }

    @Operation(tags = "Layers", summary = "Delete an existing new layer")
    @ApiResponse(responseCode = "204", description = "Successfully deleted a layer")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        layerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(tags = "Layers", summary = "Get List of available layers")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE,
        array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = PATH_SEARCH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getSummaries(@RequestBody
                                              @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                  description = "geoJSON: only layers with features intersecting with" +
                                                      " geoJSON boundary will be returned and id: EventId for" +
                                                      " EventShape layer")
                                                  LayerSummarySearchDto inputDto) {
        return layerService.getList(inputDto.getGeoJSON(), inputDto.getId())
            .stream().map(LayerSummaryDto::fromLayer)
            .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Get Layers by their ids")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE,
        array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = PATH_DETAILS, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerDetailsDto> getDetails(@RequestBody
                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                description = "geoJSON: only layers with features intersecting with" +
                                                    " geoJSON boundary will be returned; eventId: Event Id for" +
                                                    " EventShape layer; List of layer ids to retrieve")
                                                LayerDetailsSearchDto inputDto) {
        return layerService.get(inputDto.getGeoJSON(), inputDto.getLayerIds(), inputDto.getEventId())
            .stream().map(LayerDetailsDto::fromLayer)
            .collect(Collectors.toList());
    }
}
