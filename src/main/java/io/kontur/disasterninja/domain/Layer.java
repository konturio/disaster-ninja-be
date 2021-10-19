package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerCategory;
import k2layers.api.model.FeatureGeoJSON;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.kontur.disasterninja.domain.DtoFeatureProperties.*;
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
    private final List<LegendItem> legend;
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
                    (String) ((Map) f.getProperties()).get(NAME), //name //todo getProperties cast
                    (String) ((Map) f.getProperties()).get(DESCRIPTION), //description
                    OVERLAY, //category //todo field mapping
                    "",//group //todo field mapping
                    null,//legend //todo field mapping
                    (String) ((Map) f.getProperties()).get(LICENSE_URL) ,//copyright //todo field mapping - check
                    (Integer) ((Map) f.getProperties()).get(MAX_ZOOM),//maxZoom
                    (Integer) ((Map) f.getProperties()).get(MIN_ZOOM),//minZoom
                    null//source //todo field mapping
                )

                //"best": false,
                //                "category": "photo",
                //                "country_code": "CH",
                //                "description": "This imagery is provided via a proxy operated by https://sosm.ch/",
                //                "end_date": "2014", --todo do we take this into account anyhow?
                //                "start_date": "2014",
                //                "id": "Aargau-AGIS-2014",
                //                "license_url": "https://wiki.openstreetmap.org/wiki/Switzerland/AGIS",
                //                "max_zoom": 19,
                //                "min_zoom": 8,
                //                "name": "Kanton Aargau 25cm (AGIS 2014)",
                //                "privacy_policy_url": "https://sosm.ch/about/terms-of-service/",
                //                "type": "tms",
                //                "url": "https://mapproxy.osm.ch/tiles/AGIS2014/EPSG900913/{zoom}/{x}/{y}.png?origin=nw"
            )
            .collect(Collectors.toList());
        //public Layer(String id,
        //             String name,
        //             String description,
        //             LayerCategory category,
        //             String group,
        //             List<LegendItem> legend,
        //             String copyright,
        //             Double maxZoom,
        //             Double minZoom,
        //             LayerSource source)
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
            null); //todo field mapping
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
                null) //todo field mapping
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
}
