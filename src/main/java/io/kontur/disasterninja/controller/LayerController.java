package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.dto.layer.*;
import io.kontur.disasterninja.service.GeometryTransformer;
import io.kontur.disasterninja.service.layers.LayerService;
import io.kontur.disasterninja.service.layers.LayersApiService;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;

import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
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
    private final LayersApiService layersApiService;
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
        Layer layer = layersApiService.createLayer(dto);
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
        Layer layer = layersApiService.updateLayer(id, dto);
        return LayerSummaryDto.fromLayer(layer);
    }

    @Operation(tags = "Layers", summary = "Delete an existing layer")
    @ApiResponse(responseCode = "204", description = "Successfully deleted a layer")
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable String id) {
        layersApiService.deleteLayer(id);
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
            array = @ArraySchema(schema = @Schema(implementation = LayerDetailsDto.class))))
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
        FeatureCollection fc = layersApiService.updateFeatures(layerId, body);
        return ResponseEntity.ok(fc);
    }

    @Operation(tags = "Layers", summary = "Get layer feature set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Feature.class)))),
            @ApiResponse(responseCode = "204", description = "No content", content = @Content(mediaType = APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_JSON_VALUE))
    })
    @PostMapping(path = "/{id}/items/search", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Feature>> getFeatures(
            @Parameter(in = ParameterIn.PATH, description = "local identifier of a collection", required = true, example = "hotProjects")
            @PathVariable("id") String layerId,
            @RequestBody LayerItemsSearchDto inputDto) {

        LayerSearchParams searchParams;
        try {
            searchParams = createLayerSearchParams(inputDto);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage(), e);
        }

        List<Feature> features;
        Integer limit = searchParams.getLimit();

        if (limit != null) {
            if (limit <= 0) {
                throw new WebApplicationException("Limit must be greater than or equal to 1", HttpStatus.BAD_REQUEST);
            }

            // load the requested slice of features
            final int offset = searchParams.getOffset() == null ? 0 : Math.max(0, searchParams.getOffset());
            features = layersApiService.getFeatures(searchParams.getBoundary(), layerId, searchParams.getAppId(), limit, offset, searchParams.getOrder());
        } else {
            // load all features
            features = layersApiService.getAllFeatures(searchParams.getBoundary(), layerId, searchParams.getAppId());
        }

        if (features == null || features.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(emptyList());
        }

        return ResponseEntity.ok(features);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Invalid request: " + ex.getCause().getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleJsonMappingException(ValidationException ex) {
        return ResponseEntity.badRequest().body("Invalid JSON: " + ex.getCause().getMessage());
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

    private LayerSearchParams createLayerSearchParams(LayerItemsSearchDto dto) {
        return LayerSearchParams.builder()
                .appId(dto.getAppId())
                .boundary(geometryTransformer.makeValid(geometryTransformer.getGeometryFromGeoJson(dto.getGeoJSON())))
                .limit(dto.getLimit())
                .offset(dto.getOffset())
                .order(dto.getOrder())
                .build();
    }

    private record LayersAppRequestBody(UUID appId) {}
}
