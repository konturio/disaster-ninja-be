package io.kontur.disasterninja.util;

import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;

import io.kontur.disasterninja.domain.BivariateLegendAxes;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.domain.enums.LegendType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public final class TestUtil {

    public static String readFile(Object context, String file) throws IOException {
        return IOUtils.toString(context.getClass().getResourceAsStream(file), "UTF-8");
    }

    public static Legend createLegend() {
        final String PARAM_NAME = "param name";
        final String PARAM_VALUE = "qwe";
        final String OTHER_PARAM_VALUE = "asd";

        Legend legend = new Legend(LegendType.SIMPLE, null, new ArrayList<>(), new HashMap<>(),
            new BivariateLegendAxes());
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");

        //pattern step (1)
        legend.getSteps()
            .add(new LegendStep(PARAM_NAME, null, PARAM_VALUE,
                null, null, "step name",
                HEX, map, "source-layer"));
        //non-pattern step (2)
        legend.getSteps()
            .add(new LegendStep(PARAM_NAME, null, OTHER_PARAM_VALUE,
                null, null, "step name2",
                HEX, map, "source-layer"));

        return legend;
    }

}
