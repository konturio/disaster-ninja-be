package io.kontur.disasterninja.dto.layerapi;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Collection {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("copyrights")
    private String copyrights;

    @JsonProperty("properties")
    private ObjectNode properties;

    @JsonAlias("legend")
    @JsonProperty("styleRule")
    private ObjectNode styleRule;

    @JsonProperty("displayRule")
    private ObjectNode displayRule;

    @JsonProperty("group")
    private CollectionGroupProperties group;

    @JsonProperty("category")
    private CollectionCategoryProperties category;

    @JsonProperty("links")
    private List<Link> links;

    @JsonProperty("itemType")
    private String itemType;

    @JsonProperty("ownedByUser")
    private boolean ownedByUser;

    @JsonProperty("featureProperties")
    private ObjectNode featureProperties;
}
