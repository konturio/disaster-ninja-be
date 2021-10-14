package io.kontur.disasterninja.dto;

import com.google.gson.Gson;
import io.kontur.disasterninja.dto.layer.osm.OsmProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DtoJsonTest {
    Gson gson = new Gson();

    @Test
    public void testBuilder() {
        OsmProperties.OsmPropertiesBuilder builder = OsmProperties.builder();
        builder.id("qwe");

        String str = gson.toJson(builder.build());

        System.out.println(str);
        OsmProperties obj = gson.fromJson(str, OsmProperties.class);
        Assertions.assertEquals("qwe", obj.getId());
    }
}
