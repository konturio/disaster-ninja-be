package io.kontur.disasterninja.resource;

import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.service.EventApiService;
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

    @GetMapping()
    public List<EventListEventDto> getListOfEvent() {
        return service.getEvents();
    }

}
