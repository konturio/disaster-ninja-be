package io.kontur.disasterninja.dto.layerapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Link {

    private String href;
    private String rel;
    private String type;
    private String hreflang;
    private String title;
    private Integer length;
    private String apiKey;
}
