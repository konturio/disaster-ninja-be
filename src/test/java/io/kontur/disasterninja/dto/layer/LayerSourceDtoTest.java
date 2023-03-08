package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LayerSourceDtoTest {

    @Test
    public void testBasemapUrl_userLanguagePositive() {
        Layer layer = createLayer();
        layer.setSource(LayerSource.builder().urls(List.of("https://d.n/style_{lang}.json")).build());
        layer.setProperties(JsonUtil.readJson("{\"lang\":[\"kz\",\"ar\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-language", "ar");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSourceDto dto = LayerSourceDto.fromLayer(layer);

        assertNotNull(dto);
        assertEquals("https://d.n/style_ar.json", dto.urls().get(0));
    }

    @Test
    public void testBasemapUrl_NoMatchUseEn() {
        Layer layer = createLayer();
        layer.setSource(LayerSource.builder().urls(List.of("https://d.n/style_{lang}.json")).build());
        layer.setProperties(JsonUtil.readJson("{\"lang\":[\"th\",\"en\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSourceDto dto = LayerSourceDto.fromLayer(layer);

        assertNotNull(dto);
        assertEquals("https://d.n/style_en.json", dto.urls().get(0));
    }

    @Test
    public void testBasemapUrl_NoMatchAndNoSupportedEn() {
        Layer layer = createLayer();
        layer.setSource(LayerSource.builder().urls(List.of("https://d.n/style_{lang}.json")).build());
        layer.setProperties(JsonUtil.readJson("{\"lang\":[\"th\",\"kz\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSourceDto dto = LayerSourceDto.fromLayer(layer);

        assertNotNull(dto);
        assertEquals("https://d.n/style_{lang}.json", dto.urls().get(0));
    }

    @Test
    public void testBasemapUrl_NoUrl() {
        Layer layer = createLayer();
        layer.setSource(LayerSource.builder().build());
        layer.setProperties(JsonUtil.readJson("{\"lang\":[\"th\",\"kz\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSourceDto dto = LayerSourceDto.fromLayer(layer);

        assertNotNull(dto);
        assertNull(dto.urls());
    }

    @Test
    public void testBasemapUrl_NoLanguages() {
        Layer layer = createLayer();
        layer.setSource(LayerSource.builder().urls(List.of("https://d.n/style_{lang}.json")).build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSourceDto dto = LayerSourceDto.fromLayer(layer);

        assertNotNull(dto);
        assertEquals("https://d.n/style_{lang}.json", dto.urls().get(0));
    }

    private Layer createLayer() {
        return Layer.builder()
                .id("id")
                .name("name")
                .build();
    }
}
