package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.EventType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.wololo.geojson.Feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Builder
@Jacksonized
public class Layer {

    private final String id;
    private boolean globalOverlay;
    private boolean displayLegendIfNoFeaturesExist;
    private boolean boundaryRequiredForRetrieval;
    private boolean eventIdRequiredForRetrieval;
    //event shape layer only
    private EventType eventType;
    //layer summary
    private String name;
    private String description;
    private LayerCategory category;
    private String group;
    private Legend legend;
    private List<String> copyrights;
    //layer details
    private Integer maxZoom; //for 'vector' and 'raster' only (see source.type)
    private Integer minZoom; //for 'vector' and 'raster' only (see source.type)
    private LayerSource source;
    private Integer orderIndex; //for sorting bivariate presets
    private boolean ownedByUser;
    private ObjectNode featureProperties;
    private ObjectNode mapboxStyles;

    /**
     * Overrides all non-final fields with values from <b>other</b>, except for <b>this.source.data</b> as it's the
     * layer data and <b>this.legend</b> - it's merged depending on other.displayLegendIfNoFeaturesExist
     */
    public void mergeFrom(Layer other) {
        this.globalOverlay = other.isGlobalOverlay();
        this.displayLegendIfNoFeaturesExist = other.isDisplayLegendIfNoFeaturesExist();
        this.boundaryRequiredForRetrieval = other.boundaryRequiredForRetrieval;
        this.eventIdRequiredForRetrieval = other.isEventIdRequiredForRetrieval();

        if (other.orderIndex != null) {
            this.orderIndex = other.orderIndex;
        }
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
                if (!other.getLegend().getType().equals(LegendType.BIVARIATE)) {
                    this.legend = other.getLegend();
                }
            } else {
                this.legend = getLegendWithStepsForWhichFeaturesExist(other.getLegend());
            }
        }
        if (other.getCopyrights() != null) {
            this.copyrights = other.getCopyrights();
        }
        if (other.getMaxZoom() != null) {
            this.maxZoom = other.getMaxZoom();
        }
        if (other.getMinZoom() != null) {
            this.minZoom = other.getMinZoom();
        }
        LayerSource otherSource = other.getSource();
        if (otherSource != null) {
            this.source = new LayerSource(otherSource.getType(), otherSource.getTileSize(),
                    otherSource.getUrls(),
                    otherSource.getApiKey(),
                    this.source != null ? this.source.getData() : null); //sic! source.data is not replaced
        }

        this.ownedByUser = other.ownedByUser;
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
        if (prototype.getLinkProperty() != null) {
            thisLegend.setLinkProperty(prototype.getLinkProperty());
        }
        thisLegend.setSteps(new ArrayList<>());

        //return empty legend if no features exist
        if (this.getSource() == null
                || this.getSource().getData() == null
                || this.getSource().getData().getFeatures() == null) {
            return thisLegend;
        }

        if (prototype.getSteps() != null) { //sourceLayer not taken into account as its related to tiles only
            List<LegendStep> stepsToCheck = new ArrayList<>(prototype.getSteps());
            //save only steps for each at least one feature exist
            for (Feature feature : this.getSource().getData().getFeatures()) {
                if (stepsToCheck.isEmpty()) {
                    break;
                }
                for (LegendStep step : stepsToCheck) {
                    String featureValue = feature.getProperties() == null ? null
                            : (String) (feature.getProperties()).get(step.getParamName());
                    if (featureValue == null) {
                        continue;
                    }
                    String paramPattern = step.getParamPattern();
                    //if step.paramPattern is set - filter by pattern and replace feature's value with step.paramValue
                    if (paramPattern != null) {
                        if (Pattern.compile(paramPattern).matcher(featureValue).matches()) {
                            //a feature exists for this legend step
                            Optional<LegendStep> sameNameStep = thisLegend.getSteps().stream()
                                    .filter(s -> s.getStepName() != null &&
                                            s.getStepName().equals(step.getStepName()))
                                    .findFirst();
                            if (sameNameStep.isEmpty()) {
                                thisLegend.getSteps().add(step);
                            }
                            //we don't remove step from stepsToCheck as we'll need to replace matched values with step.paramValue in all further features
                            feature.getProperties().replace(step.getParamName(), step.getParamValue());
                            break;
                        }

                        //otherwise filter by paramValue
                    } else {
                        String paramValue = (String) step.getParamValue(); //nulls not allowed
                        if (paramValue.equalsIgnoreCase(featureValue)) {
                            Optional<LegendStep> sameNameStep = thisLegend.getSteps().stream()
                                    .filter(s -> s.getStepName() != null &&
                                            s.getStepName().equals(step.getStepName()))
                                    .findFirst();
                            if (sameNameStep.isEmpty()) {
                                //a feature exists for this legend step
                                thisLegend.getSteps().add(step);
                                //no need to check this legend step again
                                stepsToCheck.remove(step);
                            } else {
                                //step with the same name already added to the legend. Don't add duplication to the legend
                                //add new parameter to feature so that FE is able to recognise style rule
                                feature.getProperties().put(sameNameStep.get().getParamName(),
                                        sameNameStep.get().getParamValue());
                            }
                            break;
                        }
                    }
                }
            }
        }
        thisLegend.getSteps().sort(Comparator.comparing(LegendStep::getOrder));

        return thisLegend;
    }
}
