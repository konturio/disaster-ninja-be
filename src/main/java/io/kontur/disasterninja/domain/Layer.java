package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.dto.layer.HotProjectLayer;
import io.kontur.disasterninja.dto.layer.UrbanCoreAndSettledPeripheryLayer;
import io.kontur.disasterninja.dto.layer.osm.OsmLayer;
import lombok.Data;

import java.util.List;

@Data
public class Layer {

    private final String id;
    //layer summary
    private final String name;
    private final String description;
    private final LayerCategory category;
    private final String group;
    private final List<LegendItem> legend;
    private final String copyright;
    //layer details
    private final Double maxZoom;
    private final Double minZoom;
    private final LayerSource source;
//
//    public static Layer fromOsmLayer(OsmLayer osmLayer) {
//        return new Layer(); //todo: spec: field mapping
//    }
//
//    public static Layer fromHotProjectLayer(HotProjectLayer hotProjectLayer) {
//        return new Layer(); //todo: spec: field mapping
//    }
//
//    public static Layer fromUrbanCoreAndSettledPeripheryLayer(UrbanCoreAndSettledPeripheryLayer
//                                                                  urbanCoreAndSettledPeripheryLayer) {
//        return new Layer(); //todo: spec: field mapping
//    }

    //todo spec: OSMAnalytics TBD (#7091)
}
