package io.kontur.disasterninja.dto.layerapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionCategoryProperties {

    private String name;
    private Boolean isOpened;
    private Boolean mutuallyExclusive;
    private int order;

}
