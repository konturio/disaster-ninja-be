package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.Builder;
import lombok.Data;
import org.wololo.geojson.Feature;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Layer {
    private final String id;
    private boolean globalOverlay;
    private boolean displayLegendIfNoFeaturesExist;
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

    /**
     * Overrides all non-final fields with values from <b>other</b>, except for <b>this.source.data</b> as it's the
     * layer data and <b>this.legend</b> - it's merged depending on other.displayLegendIfNoFeaturesExist
     *
     * @param other
     * @return
     */
    public void mergeFrom(Layer other) {
        this.globalOverlay = other.isGlobalOverlay();
        this.displayLegendIfNoFeaturesExist = other.isDisplayLegendIfNoFeaturesExist();

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
            if (other.isDisplayLegendIfNoFeaturesExist()) {
                this.legend = other.getLegend();
            } else {
                this.legend = getLegendWithStepsForWhichFeaturesExist(other.getLegend());
            }
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

    /**
     * @param prototype Legend prototype
     * @return new Legend based on <b>prototype</b>, keeping name and type but using only steps for which
     * features exist in <b>this.source.data</b>
     */
    private Legend getLegendWithStepsForWhichFeaturesExist(Legend prototype) {
        final Legend thisLegend = this.getLegend() != null ? this.getLegend() : new Legend();

        if (prototype.getName() != null) {
            thisLegend.setName(prototype.getName());
        }
        if (prototype.getType() != null) {
            thisLegend.setType(prototype.getType());
        }
        thisLegend.setSteps(new ArrayList<>());

        //return empty legend if no features exist
        if (this.getSource() == null
            || this.getSource().getData() == null
            || this.getSource().getData().getFeatures() == null) {
            return thisLegend;
        }

        if (prototype.getSteps() != null) {
            List<LegendStep> stepsToCheck = new ArrayList<>(prototype.getSteps());
            for (Feature feature : this.getSource().getData().getFeatures()) {
                if (stepsToCheck.isEmpty()) {
                    break;
                }
                for (LegendStep step : stepsToCheck) {
                    String value = feature.getProperties() == null ? null
                        : (String) (feature.getProperties()).get(step.getParamName());
                    if (step.getParamValue().equalsIgnoreCase(value)) {
                        //a feature exist for this legend step
                        thisLegend.getSteps().add(step);
                        //no need to check this legend step again
                        stepsToCheck.remove(step);
                        break;
                    }
                }
            }
        }

        return thisLegend;
    }
}
