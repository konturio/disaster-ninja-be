package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Layer {
    private final String id;
    //layer summary
    private String name;
    private String description;
    private LayerCategory category;
    private String group;
    private Legend legend;
    private String copyright;
    //layer details
    private Integer maxZoom;
    private Integer minZoom;
    private LayerSource source;
}
