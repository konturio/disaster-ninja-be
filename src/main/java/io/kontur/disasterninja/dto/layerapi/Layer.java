package io.kontur.disasterninja.dto.layerapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.Legend;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Layer {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("copyrights")
    private String copyrights;

    @JsonProperty("properties")
    private Object properties;

    @JsonProperty("legend")
    private Legend legend;

    @JsonProperty("group")
    private LayerGroupProperties group;

    @JsonProperty("category")
    private LayerCategoryProperties category;

    @JsonProperty("links")
    private List<Link> links;

    @JsonProperty("itemType")
    private String itemType;

}
