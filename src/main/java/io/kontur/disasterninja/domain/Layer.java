package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerCategory;
import k2layers.api.model.FeatureGeoJSON;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
import static io.kontur.disasterninja.domain.enums.LayerCategory.BASE;
import static io.kontur.disasterninja.domain.enums.LayerCategory.OVERLAY;

@Data
public class Layer {

    private static final String HOT_ID = "hotProjects";
    private static final String HOT_NAME = "Hot Projects";

    private final String id;
    //layer summary
    private final String name;
    private final String description;
    private final LayerCategory category;
    private final String group;
    private final Legend legend;
    private final String copyright;
    //layer details
    private final Integer maxZoom;
    private final Integer minZoom;
    private final LayerSource source;

    public static List<Layer> fromOsmLayers(List<FeatureGeoJSON> dto) {
        if (dto == null) {
            return null;
        }
        return dto.stream().map(f ->
                new Layer(f.getId(), //id
                    getProperty(f, NAME, String.class), //name
                    getProperty(f, DESCRIPTION, String.class), //description
                    layerCategory(f), //category
                    caseFormat(getProperty(f, CATEGORY, String.class)),//group
                    null,//legend //todo field mapping
                    getMapValueFromProperty(f, ATTRIBUTION, TEXT, String.class),
                    getProperty(f, MAX_ZOOM, Integer.class),//maxZoom
                    getProperty(f, MIN_ZOOM, Integer.class),//minZoom
                    null//source
                )
            )
            .collect(Collectors.toList());
    }

    public static Layer fromHotProjectLayers(List<FeatureGeoJSON> dto) {
        if (dto == null) {
            return null;
        }
        //The whole collection is one layer
        return new Layer(HOT_ID,
            HOT_NAME,
            "TODO", //todo field mapping //available only at feature level
            OVERLAY,
            "", //todo field mapping
            null, //todo field mapping
            "", //todo field mapping
            null, //todo field mapping
            null, //todo field mapping
            null); //source
    }

    public static List<Layer> fromUrbanCodeAndPeripheryLayer(org.wololo.geojson.FeatureCollection dto) {
        if (dto == null) {
            return null;
        }
        return Arrays.stream(dto.getFeatures()).map(f ->
            new Layer((String) f.getId(), //id
                (String) f.getProperties().get(NAME), //name
                (String) f.getProperties().get(NAME), //description //todo field mapping
                OVERLAY, //category //todo field mapping
                "", //group //todo field mapping
                null, //legend //todo field mapping
                "", //todo field mapping
                null, //todo field mapping
                null, //todo field mapping
                null) //source
        ).collect(Collectors.toList());

        //{
        //  "type": "FeatureCollection",
        //  "features": [
        //    {
        //      "type": "Feature",
        //      "id": "kontur_settled_periphery", //the second one is "kontur_urban_core"
        //      "geometry": {
        //        "type": "MultiPolygon",
        //        "coordinates": [
        //          [
        //            [
        //              [
        //                -0.144955172,
        //                6.170015795
        //              ],
        //              [
        //                -0.083968337,
        //                6.209177849
        //              ],
        //              [
        //                2.512246911,
        //                49.914386015
        //              ]
        //            ]
        //          ]
        //        ]
        //      },
        //      "properties": {
        //        "name": "Kontur Urban Core",
        //        "totalPopulation": 150665683,
        //        "population": 102411536,
        //        "totalAreaKm2": 1631751.6,
        //        "id": "kontur_urban_core",
        //        "percentage": "0-68",
        //        "areaKm2": 139417.01
        //      }
        //    },
    }

    private static <T> T getProperty(FeatureGeoJSON f, String propertyName, Class<T> clazz) {
        Object value = f.getProperties() == null ? null : ((Map) f.getProperties()).get(propertyName);
        return value == null ? null : clazz.cast(value);
    }

    private static <T> T getMapValueFromProperty(FeatureGeoJSON f, String propertyName, Object mapKey, Class<T> clazz) {
        Map map = getProperty(f, propertyName, Map.class);
        return map == null ? null : map.get(mapKey) == null ? null : clazz.cast(map.get(mapKey));
    }

    private static LayerCategory layerCategory(FeatureGeoJSON f) {
        Boolean isOverlay = getProperty(f, DtoFeatureProperties.OVERLAY, Boolean.class);
        return (isOverlay != null && isOverlay) ? OVERLAY : BASE;
    }

    private static String caseFormat(String input) {
        return input == null ? null : input.toLowerCase().replace(input.substring(0, 1), input.substring(0, 1).toUpperCase());
    }
}
