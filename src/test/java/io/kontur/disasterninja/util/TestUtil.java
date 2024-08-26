package io.kontur.disasterninja.util;

import io.kontur.disasterninja.domain.*;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.ColorDto;
import org.apache.commons.io.IOUtils;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.Point;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;

public final class TestUtil {

    public static String readFile(Object context, String file) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(context.getClass().getResourceAsStream(file)),
                StandardCharsets.UTF_8);
    }

    public static Legend createLegend() {
        final String PARAM_NAME = "param name";
        final String PARAM_VALUE = "qwe";
        final String OTHER_PARAM_VALUE = "asd";

        BivariateLegendAxes axes = new BivariateLegendAxes();
        axes.setX(new BivariateLegendAxisDescription("xLabel", null, null, null,
                null, null, null, null));
        axes.setY(new BivariateLegendAxisDescription("yLabel", null, null, null,
                null, null, null, null));

        Legend legend = new Legend("legendName", LegendType.SIMPLE, null, new ArrayList<>(),
                Collections.singletonList(new ColorDto("A1", "rgb(232,232,157)")), axes,
                new Tooltip("markdown", "tooltipContent"));
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");

        //pattern step (1)
        legend.getSteps()
                .add(new LegendStep(PARAM_NAME, null, PARAM_VALUE,
                        null, null, "step name",
                        HEX, map, "source-layer", "fill", "stroke"));
        //non-pattern step (2)
        legend.getSteps()
                .add(new LegendStep(PARAM_NAME, null, OTHER_PARAM_VALUE,
                        null, null, "step name2",
                        HEX, map, "source-layer", "", ""));

        return legend;
    }

    public static LayerSearchParams emptyParams() {
        return LayerSearchParams.builder().build();
    }

    public static LayerSearchParams paramsWithSomeAppId() {
        return LayerSearchParams.builder().appId(UUID.randomUUID()).build();
    }

    public static LayerSearchParams paramsWithSomeBoundary() {
        Geometry someGeometry = new Point(new double[]{1d, 2d});
        return LayerSearchParams.builder().boundary(someGeometry).build();
    }

    public static LayerSearchParams someEventIdEventFeedParams() {
        return LayerSearchParams.builder().eventFeed("some-feed").eventId(UUID.randomUUID()).build();
    }
}
