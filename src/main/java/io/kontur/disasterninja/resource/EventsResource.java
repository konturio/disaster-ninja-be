package io.kontur.disasterninja.resource;

import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.service.EventApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventsResource {

    private final EventApiService service;

    public EventsResource(EventApiService service) {
        this.service = service;
    }

    @Operation(tags = "Events", summary = "Returns active events")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventListEventDto.class))))
    @GetMapping()
    public List<EventListEventDto> getListOfEvent() {
        return service.getEvents();
    }

}
