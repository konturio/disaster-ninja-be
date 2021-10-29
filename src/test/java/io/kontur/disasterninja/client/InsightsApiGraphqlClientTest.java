package io.kontur.disasterninja.client;

import com.apollographql.apollo.api.Input;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InsightsApiGraphqlClientTest {

    @Autowired
    private InsightsApiGraphqlClient client;

    @Test
    public void test(){
        String geoJsonString = """
        {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}},{"type":"Feature","properties":{},"geometry":{"type":"Polygon", "coordinates":[[[116.54296874999999,-27.215556209029675],[126.5625,-27.215556209029675],[126.5625,-23.96617587126503],[116.54296874999999,-23.96617587126503],[116.54296874999999,-27.215556209029675]]]}}, {"type": "Feature", "properties": {"url": {"report": "https://www.gdacs.org/report.aspx?eventid=1275386&episodeid=1383377&eventtype=EQ", "details": "https://www.gdacs.org/gdacsapi/api/events/geteventdata?eventtype=EQ&eventid=1275386", "geometry": "https://www.gdacs.org/gdacsapi/api/polygons/getgeometry?eventtype=EQ&eventid=1275386&episodeid=1383377"}, "icon": "https://www.gdacs.org/images/gdacs_icons/maps/Green/EQ.png", "iso3": "", "name": "Earthquake in South Indian Ocean", "Class": "Point_Centroid", "glide": "", "todate": "2021-07-01T15:06:34", "country": "South Indian Ocean", "eventid": 1275386, "fromdate": "2021-07-01T15:06:34"}, "geometry": {"type": "Point", "coordinates": [134.560546875,-22.998851594142913]}}]}";
        """;
        client.query(GeoJSONFactory.create(geoJsonString),
                Lists.newArrayList(FunctionArgs.builder().id("population0").name("sumX").x("population").build()));
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}