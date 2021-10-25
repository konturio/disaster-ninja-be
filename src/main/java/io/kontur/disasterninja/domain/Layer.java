package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Layer {
    private final String id;
    private final boolean globalOverlay;
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

    public Layer(@JsonProperty("id") String id, @JsonProperty("globalOverlay") boolean globalOverlay) {
        this.id = id;
        this.globalOverlay = globalOverlay;
    }

    /**
     * Overrides all non-final fields with values from <b>other</b>, except for <b>this.source.data</b> as it's the
     * layer data
     * @param other
     * @return
     */
    public void mergeFrom(Layer other) {
        if (other.getName() != null) {
            this.name = other.getName();
        }
        if (other.getDescription() != null) {
            this.description = other.getDescription();
        }
        if (other.getCategory() != null) {
            this.category = other.getCategory();
        }
        if (other.getGroup() != null) {
            this.group = other.getGroup();
        }
        if (other.getLegend() != null) {
            this.legend = other.getLegend(); //todo: (req:display step only if there are correspondent features) cannot be implemented without storing features themselves, see hotProjects for example
        }
        if (other.getCopyright() != null) {
            this.copyright = other.getCopyright();
        }
        if (other.getMaxZoom() != null) {
            this.maxZoom = other.getMaxZoom();
        }
        if (other.getMinZoom() != null) {
            this.minZoom = other.getMinZoom();
        }
        LayerSource otherSource = other.getSource();
        if (otherSource != null) {
            this.source = new LayerSource(otherSource.getType(), otherSource.getUrl(),
                otherSource.getTileSize(), source.getData()); //sic! source.data is not replaced
        }
    }
}
