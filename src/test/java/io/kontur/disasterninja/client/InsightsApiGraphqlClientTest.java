package io.kontur.disasterninja.client;

import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wololo.geojson.GeoJSONFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Disabled("just for local debugging")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InsightsApiGraphqlClientTest {

    @Autowired
    private InsightsApiGraphqlClient client;

    @Test
    public void testAnalytics() {
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}},{"type":"Feature","properties":{},"geometry":{"type":"Polygon", "coordinates":[[[116.54296874999999,-27.215556209029675],[126.5625,-27.215556209029675],[126.5625,-23.96617587126503],[116.54296874999999,-23.96617587126503],[116.54296874999999,-27.215556209029675]]]}}, {"type": "Feature", "properties": {"url": {"report": "https://www.gdacs.org/report.aspx?eventid=1275386&episodeid=1383377&eventtype=EQ", "details": "https://www.gdacs.org/gdacsapi/api/events/geteventdata?eventtype=EQ&eventid=1275386", "geometry": "https://www.gdacs.org/gdacsapi/api/polygons/getgeometry?eventtype=EQ&eventid=1275386&episodeid=1383377"}, "icon": "https://www.gdacs.org/images/gdacs_icons/maps/Green/EQ.png", "iso3": "", "name": "Earthquake in South Indian Ocean", "Class": "Point_Centroid", "glide": "", "todate": "2021-07-01T15:06:34", "country": "South Indian Ocean", "eventid": 1275386, "fromdate": "2021-07-01T15:06:34"}, "geometry": {"type": "Point", "coordinates": [134.560546875,-22.998851594142913]}}]}";
                """;
        List<AnalyticsTabQuery.Function> result = null;
        try {
            result = client.analyticsTabQuery(GeoJSONFactory.create(geoJsonString),
                            Lists.newArrayList(FunctionArgs.builder().id("population0").name("sumX").x("population").build()))
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    public void testAdvancedAnalytics() {
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-72.325,18.465],[-72.325,18.496],[-72.327,18.527],[-72.33,18.559],[-72.334,18.59],[-72.339,18.621],[-72.345,18.652],[-72.353,18.683],[-72.361,18.713],[-72.371,18.743],[-72.382,18.773],[-72.394,18.802],[-72.407,18.831],[-72.421,18.86],[-72.436,18.888],[-72.452,18.915],[-72.469,18.942],[-72.487,18.968],[-72.506,18.994],[-72.526,19.019],[-72.547,19.044],[-72.568,19.067],[-72.591,19.09],[-72.615,19.113],[-72.639,19.134],[-72.664,19.155],[-72.69,19.175],[-72.716,19.193],[-72.743,19.211],[-72.771,19.229],[-72.799,19.245],[-72.828,19.26],[-72.858,19.274],[-72.888,19.288],[-72.918,19.3],[-72.949,19.311],[-72.981,19.321],[-73.013,19.331],[-73.045,19.339],[-73.077,19.346],[-73.109,19.352],[-73.142,19.357],[-73.175,19.361],[-73.208,19.363],[-73.241,19.365],[-73.274,19.366],[-73.307,19.365],[-73.341,19.363],[-73.374,19.361],[-73.406,19.357],[-73.439,19.352],[-73.472,19.346],[-73.504,19.339],[-73.536,19.331],[-73.568,19.321],[-73.599,19.311],[-73.63,19.3],[-73.661,19.288],[-73.691,19.274],[-73.72,19.26],[-73.749,19.245],[-73.778,19.229],[-73.805,19.211],[-73.833,19.193],[-73.859,19.175],[-73.885,19.155],[-73.91,19.134],[-73.934,19.113],[-73.958,19.09],[-73.98,19.067],[-74.002,19.044],[-74.023,19.019],[-74.043,18.994],[-74.062,18.968],[-74.08,18.942],[-74.097,18.915],[-74.113,18.888],[-74.128,18.86],[-74.142,18.831],[-74.155,18.802],[-74.167,18.773],[-74.178,18.743],[-74.187,18.713],[-74.196,18.683],[-74.203,18.652],[-74.21,18.621],[-74.215,18.59],[-74.219,18.559],[-74.222,18.527],[-74.224,18.496],[-74.224,18.465],[-74.224,18.433],[-74.222,18.402],[-74.219,18.37],[-74.215,18.339],[-74.21,18.308],[-74.203,18.277],[-74.196,18.247],[-74.187,18.216],[-74.178,18.186],[-74.167,18.156],[-74.155,18.127],[-74.142,18.098],[-74.128,18.07],[-74.113,18.042],[-74.097,18.014],[-74.08,17.987],[-74.062,17.961],[-74.043,17.935],[-74.023,17.91],[-74.002,17.886],[-73.98,17.862],[-73.958,17.839],[-73.934,17.817],[-73.91,17.795],[-73.885,17.774],[-73.859,17.755],[-73.833,17.736],[-73.805,17.718],[-73.778,17.701],[-73.749,17.684],[-73.72,17.669],[-73.691,17.655],[-73.661,17.642],[-73.63,17.629],[-73.599,17.618],[-73.568,17.608],[-73.536,17.599],[-73.504,17.59],[-73.472,17.583],[-73.439,17.577],[-73.406,17.572],[-73.374,17.569],[-73.341,17.566],[-73.307,17.564],[-73.274,17.564],[-73.241,17.564],[-73.208,17.566],[-73.175,17.569],[-73.142,17.572],[-73.109,17.577],[-73.077,17.583],[-73.045,17.59],[-73.013,17.599],[-72.981,17.608],[-72.949,17.618],[-72.918,17.629],[-72.888,17.642],[-72.858,17.655],[-72.828,17.669],[-72.799,17.684],[-72.771,17.701],[-72.743,17.718],[-72.716,17.736],[-72.69,17.755],[-72.664,17.774],[-72.639,17.795],[-72.615,17.817],[-72.591,17.839],[-72.568,17.862],[-72.547,17.886],[-72.526,17.91],[-72.506,17.935],[-72.487,17.961],[-72.469,17.987],[-72.452,18.014],[-72.436,18.042],[-72.421,18.07],[-72.407,18.098],[-72.394,18.127],[-72.382,18.156],[-72.371,18.186],[-72.361,18.216],[-72.353,18.247],[-72.345,18.277],[-72.339,18.308],[-72.334,18.339],[-72.33,18.37],[-72.327,18.402],[-72.325,18.433],[-72.325,18.465],[-72.325,18.465]]]},"properties":{}}]}
                """;
        List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic> result = null;
        try {
            result = client.advancedAnalyticsPanelQuery(GeoJSONFactory.create(geoJsonString), null).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Assertions.assertFalse(result.isEmpty());
    }
}