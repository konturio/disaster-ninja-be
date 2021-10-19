package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.service.LayerService;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getSummaries(@Parameter @RequestBody LayerSummaryInputDto inputDto) {
        return layerService.getList(inputDto.getGeoJSON())
            .stream().map(LayerSummaryDto::fromLayer)
            .collect(Collectors.toList());
    }

//    @GetMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
//    public List<LayerDetailsDto> getDetails(@Parameter @RequestParam String eventId,
//                                            @Parameter @RequestParam String layerId) {
//        return layerService.getList() //todo use params
//            .stream().map(LayerDetailsDto::fromLayer)
//            .collect(Collectors.toList());
//    }

}
