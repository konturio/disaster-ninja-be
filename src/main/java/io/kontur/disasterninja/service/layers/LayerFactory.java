package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.domain.DtoFeatureProperties;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wololo.geojson.Feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;

@Service
public class LayerFactory {

    private static final String HOT_ID = "hotProjects";

    @Autowired
    LayerPrototypeService prototypeService;

    private static <T> T getProperty(Feature f, String propertyName, Class<T> clazz) {
        Object value = f.getProperties() == null ? null : (f.getProperties()).get(propertyName);
        return value == null ? null : clazz.cast(value);
    }

    private static <T> T getMapValueFromProperty(Feature f, String propertyName, Object mapKey, Class<T> clazz) {
        Map map = getProperty(f, propertyName, Map.class);
        return map == null ? null : map.get(mapKey) == null ? null : clazz.cast(map.get(mapKey));
    }

    private static LayerCategory layerCategory(Feature f) {
        Boolean isOverlay = getProperty(f, DtoFeatureProperties.OVERLAY, Boolean.class);
        return (isOverlay != null && isOverlay) ? OVERLAY : BASE;
    }

    private static String caseFormat(String input) {
        return input == null ? null : input.toLowerCase().replace(input.substring(0, 1), input.substring(0, 1).toUpperCase());
    }

    public List<Layer> fromOsmLayers(List<Feature> dto) {
        if (dto == null) {
            return null;
        }
        return dto.stream().map(f -> {
                Layer layer = prototypeService.prototypeOrEmpty((String) f.getId()); //todo check cast
                layer.setName(getProperty(f, NAME, String.class));
                layer.setDescription(getProperty(f, DESCRIPTION, String.class));
                layer.setCategory(layerCategory(f));
                layer.setGroup(caseFormat(getProperty(f, CATEGORY, String.class)));
                layer.setCopyright(getMapValueFromProperty(f, ATTRIBUTION, TEXT, String.class));
                    layer.setMaxZoom(getProperty(f, MAX_ZOOM, Integer.class));
                    layer.setMinZoom(getProperty(f, MIN_ZOOM, Integer.class));
                    return layer;
                }
            )
            .collect(Collectors.toList());
    }

    public Layer fromHotProjectLayers(List<Feature> dto) {
        if (dto == null) {
            return null;
        }
        //The entire collection is one layer
        return prototypeService.prototypeOrEmpty(HOT_ID);
        //todo set anything else?
    }

    public List<Layer> fromUrbanCodeAndPeripheryLayer(org.wololo.geojson.FeatureCollection dto) {
        if (dto == null) {
            return null;
        }
        return Arrays.stream(dto.getFeatures()).map(f -> {
            Layer layer = prototypeService.prototypeOrEmpty((String) f.getId());
            layer.setName((String) f.getProperties().get(NAME));
            return layer;
        }).collect(Collectors.toList());
    }
}
