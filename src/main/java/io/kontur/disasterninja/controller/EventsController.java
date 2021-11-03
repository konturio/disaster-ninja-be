package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.service.EventApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<EventListDto> getListOfEvent() {
        return service.getEvents();
    }

    @Operation(tags = "Events", summary = "Returns event by its Id")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class)))
    @ApiResponse(responseCode = "404", description = "Event is not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/{eventId}")
    public EventDto getEvent(@PathVariable UUID eventId) {
        return service.getEvent(eventId);
    }

}
