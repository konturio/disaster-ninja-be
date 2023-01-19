package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.dto.layer.*;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.LayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.wololo.geojson.FeatureCollection;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController()
@RequestMapping(LayerController.PATH)
@RequiredArgsConstructor
public class LayerController {

    public static final String PATH = "/layers";
    public static final String PATH_SEARCH = "/search";
    public static final String PATH_SEARCH_GLOBAL = "/search/global";
    public static final String PATH_SEARCH_USER = "/search/user";
    public static final String PATH_SEARCH_SELECTED_AREA = "/search/selected_area";
    public static final String PATH_DETAILS = "/details";
    private final LayerService layerService;
    private final GeometryTransformer geometryTransformer;

    @Operation(tags = "Layers", summary = "Create a new layer")
    @ApiResponse(responseCode = "200", description = "Successfully created a layer", content = @Content(
            mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LayerSummaryDto.class)))
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public LayerSummaryDto create(@RequestBody
                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                          description = "Layer data")
                                  LayerCreateDto dto) {
        Layer layer = layerService.create(dto);
        return LayerSummaryDto.fromLayer(layer);
    }

    @Operation(tags = "Layers", summary = "Update an existing layer")
    @ApiResponse(responseCode = "200", description = "Successfully updated a layer", content = @Content(
            mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = LayerSummaryDto.class)))
    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public LayerSummaryDto update(@PathVariable String id, @RequestBody
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Layer data") LayerUpdateDto dto) {
        Layer layer = layerService.update(id, dto);
        return LayerSummaryDto.fromLayer(layer);
    }

    @Operation(tags = "Layers", summary = "Delete an existing layer")
    @ApiResponse(responseCode = "204", description = "Successfully deleted a layer")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable String id) {
        layerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(tags = "Layers", summary = "Get list of global layers")
    @ApiResponse(responseCode = "200", description = "Retrieved list of global layers", content = @Content(mediaType =
            APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = PATH_SEARCH_GLOBAL, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getGlobalLayers(@RequestBody LayersAppRequestBody body) {
        UUID id = body == null ? null : body.appId;
        return layerService.getGlobalLayers(LayerSearchParams.builder().appId(id).build())
                .stream().map(LayerSummaryDto::fromLayer)
                .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Get list of user layers")
    @ApiResponse(responseCode = "200", description = "Retrieved list of user layers", content = @Content(mediaType =
            APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = PATH_SEARCH_USER, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getUserLayers(@RequestBody LayersAppRequestBody body) {
        return layerService.getUserLayers(LayerSearchParams.builder().appId(body.appId).build())
                .stream().map(LayerSummaryDto::fromLayer)
                .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Get list of selected area layers")
    @ApiResponse(responseCode = "200", description = "Retrieved list of selected area layers", content = @Content(
            mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation =
            LayerSummaryDto.class))))
    @PostMapping(path = PATH_SEARCH_SELECTED_AREA, consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getSelectedAreaLayers(@RequestBody LayerSummarySearchDto inputDto) {
        LayerSearchParams searchParams = createLayerSearchParams(inputDto);
        return layerService.getSelectedAreaLayers(searchParams)
                .stream().map(LayerSummaryDto::fromLayer)
                .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Get Layers by their ids")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = LayerSummaryDto.class))))
    @PostMapping(path = PATH_DETAILS, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerDetailsDto> getDetails(@RequestBody
                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                    description = "geoJSON boundary; eventId: Event Id for" +
                                                            " EventShape layer; List of layer ids which should be retrieved" +
                                                            " using the provided boundary; List of layer ids which" +
                                                            " should be retrieved ignoring the boundary")
                                            LayerDetailsSearchDto inputDto) {
        LayerSearchParams searchParams = createLayerSearchParams(inputDto);
        return layerService.get(inputDto.getLayersToRetrieveWithGeometryFilter(),
                        inputDto.getLayersToRetrieveWithoutGeometryFilter(), searchParams)
                .stream().map(LayerDetailsDto::fromLayer)
                .collect(Collectors.toList());
    }

    @Operation(tags = "Layers", summary = "Updates layer feature set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "fetch the set of features", content = @Content(schema = @Schema(implementation = FeatureCollection.class))),
            @ApiResponse(responseCode = "404", description = "The requested URI was not found.")})
    @PutMapping(path = "/{id}/items", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeatureCollection> updateFeatures(
            @Parameter(in = ParameterIn.PATH, description = "local identifier of a collection", required = true)
            @PathVariable("id") String layerId,
            @RequestBody FeatureCollection body) {
        FeatureCollection fc = layerService.updateFeatures(layerId, body);
        return ResponseEntity.ok(fc);
    }

    private LayerSearchParams createLayerSearchParams(LayerDetailsSearchDto dto) {
        return LayerSearchParams.builder()
                .appId(dto.getAppId())
                .eventId(dto.getEventId())
                .eventFeed(dto.getEventFeed())
                .boundary(geometryTransformer.getGeometryFromGeoJson(dto.getGeoJSON()))
                .build();
    }

    private LayerSearchParams createLayerSearchParams(LayerSummarySearchDto dto) {
        return LayerSearchParams.builder()
                .appId(dto.getAppId())
                .eventId(dto.getEventId())
                .eventFeed(dto.getEventFeed())
                .boundary(geometryTransformer.getGeometryFromGeoJson(dto.getGeoJSON()))
                .build();
    }

    private record LayersAppRequestBody(UUID appId) {}
}
