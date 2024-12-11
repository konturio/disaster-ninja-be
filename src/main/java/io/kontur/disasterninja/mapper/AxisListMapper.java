package io.kontur.disasterninja.mapper;

import io.kontur.disasterninja.domain.Transformation;
import io.kontur.disasterninja.domain.DatasetStats;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.BivariateLegendAxisStep;
import io.kontur.disasterninja.domain.BivariateLegendQuotient;
import io.kontur.disasterninja.domain.Unit;
import io.kontur.disasterninja.graphql.AxisListQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AxisListMapper {

    AxisListMapper INSTANCE = Mappers.getMapper(AxisListMapper.class);

    @Mapping(target = "transformation", expression = "java(transformation.transformation())")
    @Mapping(target = "min", expression = "java(transformation.min())")
    @Mapping(target = "mean", expression = "java(transformation.mean())")
    @Mapping(target = "skew", expression = "java(transformation.skew())")
    @Mapping(target = "stddev", expression = "java(transformation.stddev())")
    @Mapping(target = "lowerBound", expression = "java(transformation.lowerBound())")
    @Mapping(target = "upperBound", expression = "java(transformation.upperBound())")
    @Mapping(target = "points", ignore = true)
    Transformation axisListQueryTransformationToTransformation(AxisListQuery.Transformation transformation);

    @Mapping(target = "minValue", expression = "java(datasetStats.minValue())")
    @Mapping(target = "maxValue", expression = "java(datasetStats.maxValue())")
    @Mapping(target = "mean", expression = "java(datasetStats.mean())")
    @Mapping(target = "stddev", expression = "java(datasetStats.stddev())")
    DatasetStats axisListQueryDatasetStatsToDatasetStats(AxisListQuery.DatasetStats datasetStats);

    @Mapping(target = "id", expression = "java(unit.id())")
    @Mapping(target = "shortName", expression = "java(unit.shortName())")
    @Mapping(target = "longName", expression = "java(unit.longName())")
    Unit axisListQueryUnitToUnit(AxisListQuery.Unit unit);

    List<BivariateLegendAxisDescription> axisListQueryAxisListToBivariateLegendAxisDescriptionList(
            List<AxisListQuery.Axis> axisListQueryAxisList);

    @Mapping(target = "label", expression = "java(axis.label())")
    @Mapping(target = "quotient", expression = "java(axis.quotient())")
    @Mapping(target = "quotients",
            expression = "java(axisListQueryQuotientListToBivariateLegendQuotientList(axis.quotients()))")
    @Mapping(target = "steps",
            expression = "java(axisListQueryStepListToBivariateLegendAxisStepList(axis.steps()))")
    @Mapping(target = "quality", expression = "java(axis.quality())")
    @Mapping(target = "transformation", expression = "java(axisListQueryTransformationToTransformation(axis.transformation()))")
    @Mapping(target = "datasetStats", expression = "java(axisListQueryDatasetStatsToDatasetStats(axis.datasetStats()))")
    @Mapping(target = "parent", expression = "java(axis.parent())")
    BivariateLegendAxisDescription axisListQueryAxisToAxisDescription(AxisListQuery.Axis axis);

    List<BivariateLegendQuotient> axisListQueryQuotientListToBivariateLegendQuotientList(
            List<AxisListQuery.Quotient> quotients);

    @Mapping(target = "name", expression = "java(quotient.name())")
    @Mapping(target = "label", expression = "java(quotient.label())")
    @Mapping(target = "emoji", expression = "java(quotient.emoji())")
    @Mapping(target = "maxZoom", expression = "java(quotient.maxZoom())")
    @Mapping(target = "description", expression = "java(quotient.description())")
    @Mapping(target = "copyrights", expression = "java(quotient.copyrights())")
    @Mapping(target = "direction", expression = "java(quotient.direction())")
    @Mapping(target = "unit", expression = "java(axisListQueryUnitToUnit(quotient.unit()))")
    BivariateLegendQuotient axisListQueryQuotientToBivariateLegendQuotient(AxisListQuery.Quotient quotient);

    @Mapping(target = "value", expression = "java(step.value())")
    @Mapping(target = "label", expression = "java(step.label())")
    BivariateLegendAxisStep axisListQueryStepToBivariateLegendAxisStep(AxisListQuery.Step step);

    List<BivariateLegendAxisStep> axisListQueryStepListToBivariateLegendAxisStepList(
            List<AxisListQuery.Step> steps);
}
