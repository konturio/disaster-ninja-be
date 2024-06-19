package io.kontur.disasterninja.mapper;

import io.kontur.disasterninja.domain.Transformation;
import io.kontur.disasterninja.graphql.TransformationListQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TransformationListMapper {

    TransformationListMapper INSTANCE = Mappers.getMapper(TransformationListMapper.class);

    @Mapping(target = "transformation", expression = "java(transformation.transformation())")
    @Mapping(target = "min", expression = "java(transformation.min())")
    @Mapping(target = "mean", expression = "java(transformation.mean())")
    @Mapping(target = "skew", expression = "java(transformation.skew())")
    @Mapping(target = "stddev", expression = "java(transformation.stddev())")
    @Mapping(target = "lowerBound", expression = "java(transformation.lowerBound())")
    @Mapping(target = "upperBound", expression = "java(transformation.upperBound())")
    @Mapping(target = "points", expression = "java(transformation.points())")
    Transformation transformationListQueryTransformationToTransformation(TransformationListQuery.Transformation transformation);

    List<Transformation> transformationListQueryTransformationListToTransformationList(
            List<TransformationListQuery.Transformation> transformationListQueryTransformationList);

}
