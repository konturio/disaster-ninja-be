package io.kontur.disasterninja.mapper;

import io.kontur.disasterninja.domain.Unit;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AnalyticsMapper {

    @Mapping(target = "id", expression = "java(analyticsTabQueryUnit.id())")
    @Mapping(target = "shortName", expression = "java(analyticsTabQueryUnit.shortName())")
    @Mapping(target = "longName", expression = "java(analyticsTabQueryUnit.longName())")
    Unit analyticsTabQueryUnitToUnit(AnalyticsTabQuery.Unit analyticsTabQueryUnit);
}
