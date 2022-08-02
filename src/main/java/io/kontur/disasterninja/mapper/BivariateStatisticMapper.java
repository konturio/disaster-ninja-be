package io.kontur.disasterninja.mapper;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescriptionForOverlay;
import io.kontur.disasterninja.domain.BivariateLegendAxisStep;
import io.kontur.disasterninja.domain.BivariateLegendQuotient;
import io.kontur.disasterninja.dto.bivariatematrix.*;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import io.kontur.disasterninja.graphql.BivariateMatrixQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BivariateStatisticMapper {

    BivariateStatisticMapper INSTANCE = Mappers.getMapper(BivariateStatisticMapper.class);

    List<OverlayDto> bivariateLayerLegendQueryOverlayListToOverlayDtoList(
            List<BivariateLayerLegendQuery.Overlay> bivariateLayerLegendQueryOverlayList);

    @Mapping(target = "name", expression = "java(bivariateLayerLegendQueryOverlay.name())")
    @Mapping(target = "description", expression = "java(bivariateLayerLegendQueryOverlay.description())")
    @Mapping(target = "x",
            expression = "java(bivariateLayerLegendQueryXAxisToAxisDescription(bivariateLayerLegendQueryOverlay.x()))")
    @Mapping(target = "y",
            expression = "java(bivariateLayerLegendQueryYAxisToAxisDescription(bivariateLayerLegendQueryOverlay.y()))")
    @Mapping(target = "colors",
            expression = "java(bivariateLayerLegendQueryColorListToColorDtoList(bivariateLayerLegendQueryOverlay.colors()))")
    @Mapping(target = "order", expression = "java(bivariateLayerLegendQueryOverlay.order())")
    OverlayDto bivariateLayerLegendQueryOverlayToOverlayDto(
            BivariateLayerLegendQuery.Overlay bivariateLayerLegendQueryOverlay);

    @Mapping(target = "label", expression = "java(x.label())")
    @Mapping(target = "quotient", expression = "java(x.quotient())")
    @Mapping(target = "quotients",
            expression = "java(bivariateLayerLegendQueryQuotientListToBivariateLegendQuotientList(x.quotients()))")
    @Mapping(target = "steps",
            expression = "java(bivariateLayerLegendQueryStepListToBivariateLegendAxisStepList(x.steps()))")
    @Mapping(target = "quality", ignore = true)
    @Mapping(target = "parent", ignore = true)
    BivariateLegendAxisDescriptionForOverlay bivariateLayerLegendQueryXAxisToAxisDescription(BivariateLayerLegendQuery.X x);

    List<BivariateLegendAxisStep> bivariateLayerLegendQueryStepListToBivariateLegendAxisStepList(
            List<BivariateLayerLegendQuery.Step> steps);

    @Mapping(target = "value", expression = "java(step.value())")
    @Mapping(target = "label", expression = "java(step.label())")
    BivariateLegendAxisStep bivariateLayerLegendQueryStepToBivariateLegendAxisStep(BivariateLayerLegendQuery.Step step);

    List<BivariateLegendQuotient> bivariateLayerLegendQueryQuotientListToBivariateLegendQuotientList(
            List<BivariateLayerLegendQuery.Quotient> quotients);

    @Mapping(target = "name", expression = "java(quotient.name())")
    @Mapping(target = "label", expression = "java(quotient.label())")
    @Mapping(target = "direction", expression = "java(quotient.direction())")
    BivariateLegendQuotient bivariateLayerLegendQueryQuotientToBivariateLegendQuotient(
            BivariateLayerLegendQuery.Quotient quotient);

    @Mapping(target = "label", expression = "java(y.label())")
    @Mapping(target = "quotient", expression = "java(y.quotient())")
    @Mapping(target = "quotients",
            expression = "java(bivariateLayerLegendQueryQuotient1ListToBivariateLegendQuotientList(y.quotients()))")
    @Mapping(target = "steps",
            expression = "java(bivariateLayerLegendQueryStep1ListToBivariateLegendAxisStepList(y.steps()))")
    @Mapping(target = "quality", ignore = true)
    @Mapping(target = "parent", ignore = true)
    BivariateLegendAxisDescriptionForOverlay bivariateLayerLegendQueryYAxisToAxisDescription(BivariateLayerLegendQuery.Y y);

    List<BivariateLegendAxisStep> bivariateLayerLegendQueryStep1ListToBivariateLegendAxisStepList(
            List<BivariateLayerLegendQuery.Step1> steps);

    @Mapping(target = "value", expression = "java(step1.value())")
    @Mapping(target = "label", expression = "java(step1.label())")
    BivariateLegendAxisStep bivariateLayerLegendQueryStep1ToBivariateLegendAxisStep(BivariateLayerLegendQuery.Step1 step1);

    List<BivariateLegendQuotient> bivariateLayerLegendQueryQuotient1ListToBivariateLegendQuotientList(
            List<BivariateLayerLegendQuery.Quotient1> quotients);

    @Mapping(target = "name", expression = "java(quotient1.name())")
    @Mapping(target = "label", expression = "java(quotient1.label())")
    @Mapping(target = "direction", expression = "java(quotient1.direction())")
    BivariateLegendQuotient bivariateLayerLegendQueryQuotient1ToBivariateLegendQuotient(
            BivariateLayerLegendQuery.Quotient1 quotient1);

    List<ColorDto> bivariateLayerLegendQueryColorListToColorDtoList(List<BivariateLayerLegendQuery.Color> colors);

    @Mapping(target = "id", expression = "java(color.id())")
    @Mapping(target = "color", expression = "java(color.color())")
    ColorDto bivariateLayerLegendQueryColorToColorDto(BivariateLayerLegendQuery.Color color);

    List<IndicatorDto> bivariateLayerLegendQueryIndicatorListToIndicatorDtoList(
            List<BivariateLayerLegendQuery.Indicator> bivariateLayerLegendQueryIndicatorList);

    @Mapping(target = "name", expression = "java(indicator.name())")
    @Mapping(target = "label", expression = "java(indicator.label())")
    @Mapping(target = "copyrights", expression = "java(indicator.copyrights())")
    @Mapping(target = "direction", expression = "java(indicator.direction())")
    IndicatorDto bivariateLayerLegendQueryIndicatorToIndicatorDto(BivariateLayerLegendQuery.Indicator indicator);

    List<IndicatorDto> bivariateMatrixQueryIndicatorListToIndicatorDtoList(
            List<BivariateMatrixQuery.Indicator> bivariateMatrixQueryIndicatorList);

    @Mapping(target = "name", expression = "java(indicator.name())")
    @Mapping(target = "label", expression = "java(indicator.label())")
    @Mapping(target = "copyrights", expression = "java(indicator.copyrights())")
    @Mapping(target = "direction", expression = "java(indicator.direction())")
    IndicatorDto bivariateMatrixQueryIndicatorToIndicatorDto(BivariateMatrixQuery.Indicator indicator);

    List<BivariateLegendAxisDescription> bivariateMatrixQueryAxisListToBivariateLegendAxisDescriptionList(
            List<BivariateMatrixQuery.Axis> bivariateMatrixQueryAxisList);

    @Mapping(target = "label", expression = "java(axis.label())")
    @Mapping(target = "quotient", expression = "java(axis.quotient())")
    @Mapping(target = "steps",
            expression = "java(bivariateMatrixQueryStepListToBivariateLegendAxisStepList(axis.steps()))")
    @Mapping(target = "quality", expression = "java(axis.quality())")
    @Mapping(target = "parent", expression = "java(axis.parent())")
    BivariateLegendAxisDescription bivariateMatrixQueryAxisToAxisDescription(BivariateMatrixQuery.Axis axis);

    List<BivariateLegendAxisStep> bivariateMatrixQueryStepListToBivariateLegendAxisStepList(
            List<BivariateMatrixQuery.Step> steps);

    @Mapping(target = "value", expression = "java(step.value())")
    @Mapping(target = "label", expression = "java(step.label())")
    BivariateLegendAxisStep bivariateMatrixQueryStepToBivariateLegendAxisStep(BivariateMatrixQuery.Step step);

    @Mapping(target = "minZoom", expression = "java(bivariateMatrixQueryMeta.min_zoom())")
    @Mapping(target = "maxZoom", expression = "java(bivariateMatrixQueryMeta.max_zoom())")
    MetaDto bivariateMatrixQueryMetaToMetaDto(BivariateMatrixQuery.Meta bivariateMatrixQueryMeta);

    List<CorrelationRateDto> bivariateMatrixQueryCorrelationRateListToCorrelationRateDtoList(
            List<BivariateMatrixQuery.CorrelationRate> bivariateMatrixQueryCorrelationRateList);

    @Mapping(target = "x", expression = "java(bivariateMatrixQueryXAxisToAxisDescription(correlationRate.x()))")
    @Mapping(target = "y", expression = "java(bivariateMatrixQueryYAxisToAxisDescription(correlationRate.y()))")
    @Mapping(target = "rate", expression = "java(correlationRate.rate())")
    @Mapping(target = "quality", expression = "java(correlationRate.quality())")
    @Mapping(target = "correlation", expression = "java(correlationRate.correlation())")
    @Mapping(target = "avgCorrelationX", expression = "java(correlationRate.avgCorrelationX())")
    @Mapping(target = "avgCorrelationY", expression = "java(correlationRate.avgCorrelationY())")
    CorrelationRateDto bivariateMatrixQueryCorrelationRateToCorrelationRateDto(
            BivariateMatrixQuery.CorrelationRate correlationRate);

    @Mapping(target = "label", expression = "java(x.label())")
    @Mapping(target = "quotient", expression = "java(x.quotient())")
    @Mapping(target = "steps",
            expression = "java(bivariateMatrixQueryStep1ListToBivariateLegendAxisStepList(x.steps()))")
    @Mapping(target = "quality", expression = "java(x.quality())")
    @Mapping(target = "parent", expression = "java(x.parent())")
    BivariateLegendAxisDescription bivariateMatrixQueryXAxisToAxisDescription(BivariateMatrixQuery.X x);

    List<BivariateLegendAxisStep> bivariateMatrixQueryStep1ListToBivariateLegendAxisStepList(
            List<BivariateMatrixQuery.Step1> steps);

    @Mapping(target = "value", expression = "java(step.value())")
    @Mapping(target = "label", expression = "java(step.label())")
    BivariateLegendAxisStep bivariateMatrixQueryStep1ToBivariateLegendAxisStep(BivariateMatrixQuery.Step1 step);

    @Mapping(target = "label", expression = "java(y.label())")
    @Mapping(target = "quotient", expression = "java(y.quotient())")
    @Mapping(target = "steps",
            expression = "java(bivariateMatrixQueryStep2ListToBivariateLegendAxisStepList(y.steps()))")
    @Mapping(target = "quality", expression = "java(y.quality())")
    @Mapping(target = "parent", expression = "java(y.parent())")
    BivariateLegendAxisDescription bivariateMatrixQueryYAxisToAxisDescription(BivariateMatrixQuery.Y y);

    List<BivariateLegendAxisStep> bivariateMatrixQueryStep2ListToBivariateLegendAxisStepList(
            List<BivariateMatrixQuery.Step2> steps);

    @Mapping(target = "value", expression = "java(step.value())")
    @Mapping(target = "label", expression = "java(step.label())")
    BivariateLegendAxisStep bivariateMatrixQueryStep2ToBivariateLegendAxisStep(BivariateMatrixQuery.Step2 step);

    @Mapping(target = "fallback", expression = "java(bivariateMatrixQueryColors.fallback())")
    @Mapping(target = "combinations",
            expression = "java(bivariateMatrixQueryCombinationListToCombinationDtoList(bivariateMatrixQueryColors.combinations()))")
    ColorsDto bivariateMatrixQueryColorsToColorsDto(BivariateMatrixQuery.Colors bivariateMatrixQueryColors);

    List<CombinationDto> bivariateMatrixQueryCombinationListToCombinationDtoList(
            List<BivariateMatrixQuery.Combination> bivariateMatrixQueryCombinations);

    @Mapping(target = "color", expression = "java(bivariateMatrixQueryCombination.color())")
    @Mapping(target = "corner", expression = "java(bivariateMatrixQueryCombination.corner())")
    @Mapping(target = "colorComment", expression = "java(bivariateMatrixQueryCombination.color_comment())")
    CombinationDto bivariateMatrixQueryCombinationToCombinationDto(BivariateMatrixQuery.Combination bivariateMatrixQueryCombination);
}
