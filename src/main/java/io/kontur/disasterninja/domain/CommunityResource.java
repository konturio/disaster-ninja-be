package io.kontur.disasterninja.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CommunityResource {
    private String id;
    private Map<String, Object> strings;
    private Map<String, Object> resolved;
    //"id": "OSM-Colorado",
    //            "type": "meetup",
    //            "account": "OSM-Colorado",
    //            "locationSet": {"include": ["colorado.geojson"]},
    //            "languageCodes": ["en"],
    //            "order": 5,
    //            "strings": {
    //              "name": "OpenStreetMap Colorado",
    //              "description": "Mappers and OpenStreetMap users in the state of Colorado, USA",
    //              "extendedDescription": "OpenStreetMap (OSM) Colorado is a local collaboration of people interested in contributing their efforts to create free maps. We encourage all of our mappers to organize or suggest mapping events throughout the state. Meetup activities can be simple social mixers, OSM basic to advanced training, or community mapping parties."
    //            },
    //            "contacts": [
    //              {"name": "Diane Fritz", "email": "frizatch@gmail.com"}
    //            ],
    //            "resolved": {
    //              "name": "OpenStreetMap Colorado",
    //              "url": "https://meetup.com/OSM-Colorado",
    //              "description": "Mappers and OpenStreetMap users in the state of Colorado, USA",
    //              "extendedDescription": "OpenStreetMap (OSM) Colorado is a local collaboration of people interested in contributing their efforts to create free maps. We encourage all of our mappers to organize or suggest mapping events throughout the state. Meetup activities can be simple social mixers, OSM basic to advanced training, or community mapping parties.",
    //              "nameHTML": "<a target=\"_blank\" href=\"https://meetup.com/OSM-Colorado\">OpenStreetMap Colorado</a>",
    //              "urlHTML": "<a target=\"_blank\" href=\"https://meetup.com/OSM-Colorado\">https://meetup.com/OSM-Colorado</a>",
    //              "descriptionHTML": "Mappers and OpenStreetMap users in the state of Colorado, USA",
    //              "extendedDescriptionHTML": "OpenStreetMap (OSM) Colorado is a local collaboration of people interested in contributing their efforts to create free maps. We encourage all of our mappers to organize or suggest mapping events throughout the state. Meetup activities can be simple social mixers, OSM basic to advanced training, or community mapping parties."
    //            }
}
