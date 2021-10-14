package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.service.LayerService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
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

    @PostConstruct
    void init() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(GeoJSON.class, new GeoJSONDeserializer(GeoJSON.class));
        objectMapper.registerModule(simpleModule);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<LayerSummaryDto> getSummaries(@Parameter @RequestBody LayerSummaryInputDto inputDto) {
        return layerService.getList(inputDto.getGeoJSON(), inputDto.getId())
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


    public static class GeoJSONDeserializer extends StdDeserializer<GeoJSON> { //todo separate file
        public GeoJSONDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public GeoJSON deserialize(JsonParser p, DeserializationContext ctxt) throws IOException  {
            return GeoJSONFactory.create(ctxt.readTree(p).toString());
        }
    }

}
