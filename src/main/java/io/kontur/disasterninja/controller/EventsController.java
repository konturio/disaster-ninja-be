package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.controller.validation.ValidBbox;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.service.EventApiService;
import io.kontur.disasterninja.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsController {

    private final EventApiService service;
    private final UserProfileService userProfileService;

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
            int offset,
            @Parameter(description = "Event API Feed name", example = "kontur-public")
            @RequestParam(required = false)
            String feed,
            @Parameter(description = "Only events that have a geometry that intersects the bounding box are selected. The bounding box is provided as four numbers" +
                    "<ul><li>Lower left corner, coordinate axis 1</li>" +
                    "<li>Lower left corner, coordinate axis 2</li>" +
                    "<li>Upper right corner, coordinate axis 1</li>" +
                    "<li>Upper right corner, coordinate axis 2</li></ul>" +
                    "The coordinate reference system of the values is WGS 84 longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84). For WGS 84 longitude/latitude the values are the sequence of minimum longitude, minimum latitude, maximum longitude and maximum latitude.")
            @RequestParam(required = false)
            @ValidBbox
            List<BigDecimal> bbox
    ) {
        List<EventListDto> events = service.getEvents(feed, bbox);
        if (offset >= events.size()) {
            throw new WebApplicationException("Offset is larger than resultset size", HttpStatus.NO_CONTENT);
        }
        int maxIndex = offset + limit;
        if (events.size() < maxIndex) {
            maxIndex = events.size();
        }
        return events.subList(offset, maxIndex);
    }

    @Operation(tags = "Events", summary = "Returns event by its Id")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class)))
    @ApiResponse(responseCode = "404", description = "Event is not found", content = @Content(mediaType = "application/json"))
    @GetMapping("/{feed}/{eventId}")
    public EventDto getEvent(@PathVariable UUID eventId, @PathVariable String feed) {
        return service.getEvent(eventId, feed);
    }

    @Operation(tags = "Events", summary = "Returns list of feeds available to user by username resolved from JWT token"
            + ". The one which has isDefault=true is the one chosen by user to be displayed")
    @ApiResponse(responseCode = "200", description = "Successful operation",
            content = @Content(mediaType = "application/json", array =
            @ArraySchema(schema = @Schema(implementation = EventFeedDto.class))))
    @GetMapping("/user_feeds")
    public List<EventFeedDto> getUserFeeds() {
        String defaultFeedName = userProfileService.getUserDefaultFeed();
        List<EventFeedDto> feeds = service.getUserFeeds();

        if (defaultFeedName != null && !defaultFeedName.isBlank()) {
            for (EventFeedDto feed : feeds) {
                if (defaultFeedName.equals(feed.getFeed())) {
                    feed.setDefault(true);
                    break;
                }
            }
        } else if (!feeds.isEmpty()) {
            feeds.get(0).setDefault(true);
        }
        return feeds;
    }
}
