package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.service.EventApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventApiService service;

    public EventsController(EventApiService service) {
        this.service = service;
    }

    @Operation(tags = "Events", summary = "Returns active events")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventListDto.class))))
    @GetMapping()
    public List<EventListDto> getListOfEvent(
        @Parameter(description = "Number of records on the page. Default value is 1000, minimum - 1, maximum - 1000", example = "1000", schema = @Schema(minimum = "1", maximum = "1000"))
        @RequestParam(defaultValue = "1000")
        @Min(1)
        @Max(1000)
            int limit,

        @Parameter(description = "Offset. Default value is 0, minimum - 0", example = "0", schema = @Schema(minimum = "0"))
        @RequestParam(defaultValue = "0")
        @Min(0)
            int offset
    ) {
        List<EventListDto> events = service.getEvents();
        if (offset >= events.size()) {
            throw new WebApplicationException("Offset is larger than resultset size", HttpStatus.NO_CONTENT);
        }
        return events.subList(offset, offset + limit);
    }

    @Operation(tags = "Events", summary = "Returns event by its Id")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class)))
    @ApiResponse(responseCode = "404", description = "Event is not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/{eventId}")
    public EventDto getEvent(@PathVariable UUID eventId) {
        return service.getEvent(eventId);
    }

}
